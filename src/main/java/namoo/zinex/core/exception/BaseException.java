package namoo.zinex.core.exception;


import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class BaseException extends RuntimeException{

    private final String code;
    private final String message;
    private final String detailMessage;
    private final HttpStatusCode statusCode;

    public BaseException(String code, String message, HttpStatusCode statusCode, String... detailMessage) {
        super(message);
        this.code = code;
        this.message = message;
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
