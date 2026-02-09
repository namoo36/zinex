package namoo.zinex.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import namoo.zinex.security.exception.JwtAuthenticationException;
import namoo.zinex.user.enums.Role;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

///  AccessToken(JWT)을 검증하고, 인증된 {@link JwtAuthenticationToken}을 생성하는 Provider.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final JwtTokenService jwtTokenService;
  private final AccessTokenBlacklistService accessTokenBlacklistService;

  /// 인증 요청 처리
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!(authentication instanceof JwtAuthenticationToken beforeToken)) {
      return null;
    }

    String accessToken = beforeToken.getAccessToken();
    if (accessToken == null || accessToken.isBlank()) {
      throw new BadCredentialsException("Missing access token");
    }

    // 1) accessToken 검증(서명/만료 등) - 실패 시 JwtAuthenticationException(AuthenticationException) 발생
    jwtTokenService.validateToken(accessToken);

    // 2) Claims 추출 -> userId/role 파싱
    Claims claims = jwtTokenService.getClaimsFromToken(accessToken);
    Long userId = jwtTokenService.getUserIdFromToken(claims);
    Role role = jwtTokenService.getRoleFromToken(claims);
    String jti = jwtTokenService.getJtiFromToken(claims);

    if (userId == null || role == null) {
      throw new BadCredentialsException("Invalid token claims");
    }

    // 2-1) 블랙리스트 체크(로그아웃 즉시 무효화)
    if (jti != null && accessTokenBlacklistService.isBlacklisted(jti)) {
      throw new JwtAuthenticationException("BLACKLISTED_TOKEN", "Blacklisted access token");
    }

    // 3) 인증된 Authentication 반환
    return JwtAuthenticationToken.afterOf(userId, role, claims);
  }

  /// JwtAuthenticationToken으로 들어온 인증 요청을 처리하겠다고 선언
  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}

