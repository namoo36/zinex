package namoo.zinex.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 인증(Access/Refresh 검증) 과정에서 발생하는 예외.
 *
 * <p>Spring Security 인증 실패 흐름(401)과 자연스럽게 연결하기 위해 {@link AuthenticationException}을 상속합니다.
 */
public class JwtAuthenticationException extends AuthenticationException {

  private final String errorCode;

  public JwtAuthenticationException(String message) {
    this(null, message, null);
  }

  public JwtAuthenticationException(String errorCode, String message) {
    this(errorCode, message, null);
  }

  public JwtAuthenticationException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}

