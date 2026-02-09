package namoo.zinex.core.exception;

import lombok.extern.slf4j.Slf4j;
import namoo.zinex.core.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice   // 컨트롤러 전역 예외 처리기
public class BaseExceptionHandler {

    ///  BaseException 예외 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<APIResponse<Object>> handleBaseException(BaseException ex) {

        String code = ex.getCode();
        String message = ex.getMessage();
        String detailMessage = ex.getDetailMessage();
        HttpStatusCode statusCode = ex.getStatusCode();

        String finalMessage = detailMessage != null ? message + " " + detailMessage : message;
        APIResponse<Object> body = APIResponse.nok(statusCode, code, finalMessage);

        log.error("{}: {}", statusCode, finalMessage);

        return ResponseEntity.status(statusCode).body(body);
    }

    ///  인증 실패 -> 무조건 401이기 때문에 ResponseEntity가 아닌 RepsonseStatus 사용
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public APIResponse<Object> handleAuthenticationException(AuthenticationException ex) {
        log.error("{}: {}", HttpStatus.UNAUTHORIZED, ex.getMessage());

        return APIResponse.nok(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "권한이 없습니다.");
    }

    /// 인가 실패(권한 부족) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public APIResponse<Object> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("{}: {}", HttpStatus.FORBIDDEN, ex.getMessage());
        return APIResponse.nok(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다.");
    }

    /// 요청 검증 실패(Bean Validation) -> 400
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Object> handleValidationException(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else if (ex instanceof BindException be) {
            message = be.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else {
            message = "요청 값이 올바르지 않습니다.";
        }

        log.warn("{}: {}", HttpStatus.BAD_REQUEST, message);
        return APIResponse.nok(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    /// 정적 리소스 404 (예: swagger-ui asset 경로 오류)
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public APIResponse<Object> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("{}: {}", HttpStatus.NOT_FOUND, ex.getMessage());
        return APIResponse.nok(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다.");
    }

    /// Content-Type 미지원(예: text/plain으로 JSON DTO 요청)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public APIResponse<Object> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("{}: {}", HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
        return APIResponse.nok(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            "Content-Type이 올바르지 않습니다. application/json 으로 요청해주세요."
        );
    }

    ///  그 외 예외
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Object> handleInternalServerException(Exception ex) {
        String errorMessage = "서버 내부 오류가 발생하였습니다.";
        log.error(errorMessage, ex);

        return APIResponse.nok(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", errorMessage);
    }
}
