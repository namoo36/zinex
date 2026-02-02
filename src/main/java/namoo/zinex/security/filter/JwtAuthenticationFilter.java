package namoo.zinex.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import namoo.zinex.security.exception.JwtAuthenticationException;
import namoo.zinex.security.jwt.JwtAuthenticationToken;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

///  AccessToken(Bearer) 인증 필터
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final String BEARER_PREFIX = "Bearer ";    // Authorization 헤더의 표준 prefix
  private final AuthenticationFailureHandler failureHandler;    // 인증 실패 시 JSON 401 응답 통일

  public JwtAuthenticationFilter(RequestMatcher matcher, AuthenticationFailureHandler failureHandler) {
    super(matcher);
    this.failureHandler = failureHandler;
    setAuthenticationFailureHandler(failureHandler);
  }

  /// 인증 시도
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {

    // header가 유효하지 않은 경우
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
      throw new JwtAuthenticationException("AUTH_HEADER_INVALID", "Authorization Header is invalid");
    }

    // AccessToken이 유효하지 않은 경우
    String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
    if (accessToken.isEmpty()) {
      throw new JwtAuthenticationException("MISSING_TOKEN", "Missing access token");
    }

    // 토큰 인증
    JwtAuthenticationToken beforeToken = JwtAuthenticationToken.beforeOf(accessToken);
    return this.getAuthenticationManager().authenticate(beforeToken);
  }

  ///  인증 성공 후 후처리
  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult
  ) throws IOException, ServletException {

    // SecurityContextHolder에 인증 사용자 넣기
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authResult);
    SecurityContextHolder.setContext(context);

    // 다음 필터로 진행
    chain.doFilter(request, response);
  }

  /// 실패 시
  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException failed
  ) throws IOException, ServletException {
    SecurityContextHolder.clearContext();
    // JSON 응답 통일
    failureHandler.onAuthenticationFailure(request, response, failed);
  }
}

