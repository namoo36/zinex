package namoo.zinex.user.exception;

import namoo.zinex.core.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Users(회원) 도메인에서 사용하는 비즈니스 예외.
 *
 * <p>인증 실패(로그인 실패 등)는 {@code AuthenticationException} 계열로 처리하고,
 * 그 외 도메인/비즈니스 오류(중복 이메일 등)는 {@link BaseException} 계열로 처리합니다.
 */
public class UsersException extends BaseException {

  public UsersException(String code, String message, HttpStatus status, String... detailMessage) {
    super(code, message, status, detailMessage);
  }

  public static UsersException duplicateEmail(String email) {
    return new UsersException(
        "USERS_01",
        "이미 사용 중인 이메일입니다.",
        HttpStatus.CONFLICT,
        "email=" + email
    );
  }

  public static UsersException userNotFound(String email) {
    return new UsersException(
        "USERS_02",
        "사용자가 존재하지 않습니다.",
        HttpStatus.NOT_FOUND,
        "email=" + email
    );
  }

  public static UsersException userNotFound(Long userId) {
    return new UsersException(
        "USERS_02",
        "사용자가 존재하지 않습니다.",
        HttpStatus.NOT_FOUND,
        "userId=" + userId
    );
  }

  public static UsersException invalidCredentials() {
    return new UsersException(
        "USERS_03",
        "이메일 또는 비밀번호가 올바르지 않습니다.",
        HttpStatus.UNAUTHORIZED
    );
  }
}

