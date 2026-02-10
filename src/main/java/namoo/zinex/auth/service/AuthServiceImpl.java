package namoo.zinex.auth.service;

import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import namoo.zinex.account.domain.Accounts;
import namoo.zinex.account.repository.AccountsRepository;
import namoo.zinex.auth.dto.SignUpRequest;
import namoo.zinex.auth.dto.SignUpResponse;
import namoo.zinex.auth.repository.AuthRepository;
import namoo.zinex.security.dto.JwtToken;
import namoo.zinex.security.exception.JwtAuthenticationException;
import namoo.zinex.security.jwt.AccessTokenBlacklistService;
import namoo.zinex.security.jwt.JwtTokenService;
import namoo.zinex.user.domain.Users;
import namoo.zinex.user.exception.UsersException;
import namoo.zinex.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static namoo.zinex.core.config.GlobalVariables.SESSION_ID_CLAIM_KEY;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

  private final AuthRepository authRepository;
  private final UserRepository userRepository;
  private final AccountsRepository accountsRepository;
  private final AccessTokenBlacklistService accessTokenBlacklistService;
  private final JwtTokenService jwtTokenService;
  private final PasswordEncoder passwordEncoder;

  @Value("${jwt.refresh-ttl}")
  private Duration refreshTtl;

  // RefreshToken 정보 저장 객체
  private record RefreshSession(Long userId, String sessionId) {}

  /// JWT 검증 + Claims 추출
  private Claims getValidatedClaims(String token) {
    jwtTokenService.validateToken(token);
    return jwtTokenService.getClaimsFromToken(token);
  }

  ///  Refresh Token 파싱
  private RefreshSession parseRefreshSession(String refreshToken) {
    Claims claims = getValidatedClaims(refreshToken);

    // claims에서 userId, sessionId 추출
    Long userId = jwtTokenService.getUserIdFromToken(claims);
    String sessionId = claims.get(SESSION_ID_CLAIM_KEY, String.class);

    if (userId == null || sessionId == null || sessionId.isBlank()) {
      throw new JwtAuthenticationException("INVALID_REFRESH", "Invalid refresh token claims");
    }

    return new RefreshSession(userId, sessionId);
  }

  /// 회원 가입
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public SignUpResponse signup(SignUpRequest request) {

    String email = request.email();

    if (userRepository.existsByEmail(email)) {
      throw UsersException.duplicateEmail(email);
    }

    // user 생성 & 저장
    Users user = Users.createUser(email, passwordEncoder.encode(request.password()), request.name());
    Users saved = userRepository.save(user);

    // 계좌 자동 생성(충전식 예치금 모델)
    accountsRepository.save(Accounts.createAccounts(user));

    return SignUpResponse.of(saved.getId(), saved.getEmail(), saved.getName());
  }

  ///  로그인
  @Override
  public JwtToken login(String email, String rawPassword) {
    // Email 확인
    Users user = userRepository.findByEmail(email)
        .orElseThrow(() -> UsersException.userNotFound(email));

    // 비밀번호 확인
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
        throw UsersException.invalidCredentials();
    }

    // accessToken 생성
    String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getName(), user.getRole());

    // RefreshToken 생성(sessionID 같이 저장)
    String sessionId = UUID.randomUUID().toString();
    String refreshToken = jwtTokenService.createRefreshToken(user.getId(), sessionId);

    // Reddis에 RefreshToken 저장
    authRepository.saveRefreshToken(user.getId(), sessionId, refreshToken, refreshTtl);

    return JwtToken.of(accessToken, refreshToken);
  }

  /// RefreshToken 재발급
  @Override
  public JwtToken refresh(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new JwtAuthenticationException("MISSING_REFRESH", "Missing refresh token");
    }

    // RefreshToken에서 userId, sessionId 추출
    RefreshSession session = parseRefreshSession(refreshToken);

    // Redis에 저장되어 있는 RefreshToken 추출(saved)
    String saved = authRepository.getRefreshToken(session.userId(), session.sessionId());

    if (saved == null || saved.isBlank()) {
      throw new JwtAuthenticationException("REFRESH_NOT_FOUND", "Refresh token not found");
    }

    if (!saved.equals(refreshToken)) {
      authRepository.deleteRefreshToken(session.userId(), session.sessionId());
      throw new JwtAuthenticationException("REFRESH_REUSE_DETECTED", "Refresh token reuse detected");
    }

    Users user = userRepository.findById(session.userId())
        .orElseThrow(() -> UsersException.userNotFound(session.userId()));

    // 새로운 accessToken, RefreshToken 발급
    String newAccess = jwtTokenService.createAccessToken(user.getId(), user.getName(), user.getRole());
    String newRefresh = jwtTokenService.createRefreshToken(user.getId(), session.sessionId());

    // Redis에 새로운 RefreshToken 저장
    authRepository.saveRefreshToken(user.getId(), session.sessionId(), newRefresh, refreshTtl);

    return JwtToken.of(newAccess, newRefresh);
  }

  ///  로그아웃
  @Override
  public void logout(String refreshToken, String accessToken) {

    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    // RefreshToken에서 userId, sessionId 추출 -> Redis에서 삭제
    RefreshSession session = parseRefreshSession(refreshToken);
    authRepository.deleteRefreshToken(session.userId(), session.sessionId());

    // AccessToken 블랙리스트 등록: 만료 전까지 즉시 무효화
    if (accessToken != null && !accessToken.isBlank()) {
      try {
        Claims accessClaims = getValidatedClaims(accessToken);

        // accessToken에서 정보 추출
        String jti = jwtTokenService.getJtiFromToken(accessClaims);
        long expMs = jwtTokenService.getExpirationMillisFromToken(accessClaims);
        long ttlMs = expMs - System.currentTimeMillis();
        if (ttlMs > 0) {
          // 남은 ttl만큼 유효 시간으로 지정, Redis에 저장
          accessTokenBlacklistService.blacklist(jti, Duration.ofMillis(ttlMs));
        }
      } catch (JwtAuthenticationException ignored) {
        // 이미 accessToken이 만료/유효하지 않은 경우 -> 등록 못하면 그냥 skip 하도록
      }
    }
  }
}

