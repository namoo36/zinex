package namoo.zinex.core.exception;


import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class BaseException extends RuntimeException{

    private final String code;  // 비즈니스 에러 코드
    private final String detailMessage; // 상세 설명
    private final HttpStatusCode statusCode;    // HTTP 상태 코드

    public BaseException(String code, String message, HttpStatusCode statusCode, String... detailMessage) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
        this.detailMessage = joinDetailMessages(detailMessage);
    }

    private String joinDetailMessages(String... messages) {
        if (messages == null || messages.length == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (String msg : messages) {
            if (msg != null && !msg.isBlank()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(msg);
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

}
