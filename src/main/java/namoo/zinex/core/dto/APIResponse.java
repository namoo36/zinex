package namoo.zinex.core.dto;


import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class APIResponse<T> {

    private int statusCode;
    private String code;
    private String message;
    private T data;

    private APIResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.code = "success";
        this.message = "요청에 성공했습니다";
    }

    private APIResponse(int statusCode, String code, String message, T data) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> APIResponse<T> ok() {
        return new APIResponse<>(HttpStatus.OK.value(), null);
    }

    public static <T> APIResponse<T> ok(T data) {
        return new APIResponse<>(HttpStatus.OK.value(), data);
    }

    public static <T> APIResponse<T> created(T data) {
        return new APIResponse<>(HttpStatus.CREATED.value(), data);
    }

    public static <T> APIResponse<T> created(HttpServletResponse response, T data) {
        response.setStatus(HttpStatus.CREATED.value());
        return new APIResponse<>(HttpStatus.CREATED.value(), data);
    }

    public static <T> APIResponse<T> nok(HttpStatusCode statusCode, String errorCode, String message) {
        return new APIResponse<>(statusCode.value(), errorCode, message, null);
    }

}
