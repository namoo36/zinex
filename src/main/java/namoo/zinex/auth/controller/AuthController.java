package namoo.zinex.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import namoo.zinex.auth.dto.SignInRequest;
import namoo.zinex.auth.dto.SignInResponse;
import namoo.zinex.auth.dto.SignUpRequest;
import namoo.zinex.auth.dto.SignUpResponse;
import namoo.zinex.auth.dto.TokenReissueResponse;
import namoo.zinex.auth.service.AuthService;
import namoo.zinex.core.dto.APIResponse;
import namoo.zinex.security.dto.JwtToken;
import namoo.zinex.security.jwt.JwtTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static namoo.zinex.core.config.GlobalVariables.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtTokenService jwtTokenService;

  ///  회원 가입
  @PostMapping("/signup")
  public ResponseEntity<APIResponse<SignUpResponse>> signup(
      @Valid @RequestBody SignUpRequest request) {

    SignUpResponse result = authService.signup(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(APIResponse.created(result));
  }

  ///  로그인
  @PostMapping("/signin")
  public ResponseEntity<APIResponse<SignInResponse>> signIn(
      @Valid @RequestBody SignInRequest request,
      HttpServletResponse response) {

    // token 발급
    JwtToken token = authService.login(request.email(), request.password());
    // refreshToken Cookie 저장
    jwtTokenService.setRefreshTokenCookie(response, token.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "no-store")    // 인증 응답 캐시 방지
        .header(HttpHeaders.PRAGMA, "no-cache")
        .body(APIResponse.ok(SignInResponse.of(token.accessToken(), token.refreshToken())));
  }

  ///  로그아웃
  @PostMapping("/signout")
  public APIResponse<Void> signOut(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      HttpServletResponse response
  ) {

    // Header에서 accessToken 추출
    String accessToken = null;
    if (authorization != null && authorization.startsWith("Bearer ")) {
      accessToken = authorization.substring("Bearer ".length()).trim();
    }

    if (refreshToken != null && !refreshToken.isBlank()) {
      authService.logout(refreshToken, accessToken);
    }

    // cookie에서 RefreshToken 삭제
    jwtTokenService.clearRefreshTokenCookie(response);

    return APIResponse.ok();
  }

  ///  AccessToken 토큰 재발급 (+ RefreshToken 갱신)
  @PostMapping("/reissue")
  public ResponseEntity<APIResponse<TokenReissueResponse>> reissueToken(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {

    JwtToken token = authService.refresh(refreshToken);

    if (token.refreshToken() != null && !token.refreshToken().isBlank()) {
      jwtTokenService.setRefreshTokenCookie(response, token.refreshToken());
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header(HttpHeaders.PRAGMA, "no-cache")
        .body(APIResponse.ok(TokenReissueResponse.of(token.accessToken())));
  }
}

