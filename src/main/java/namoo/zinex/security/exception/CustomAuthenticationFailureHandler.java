package namoo.zinex.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import namoo.zinex.core.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/// 인증 실패 시 JSON(APIResponse)으로 응답하는 FailureHandler -> Security Filter에서 바로 응답
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

  ///  APIResponse 객체를 JSON 문자열로 직렬화
  private final ObjectMapper objectMapper;

  ///  로그인 실패, JWT 검증 실패 -> Controller가 아닌 Filter 단게에서 호출
  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException, ServletException {

    String code = "AUTH_FAILED";
    String message = "인증에 실패했습니다.";   // 메시지 일관화

    if (exception instanceof JwtAuthenticationException jwtEx) {
      if (jwtEx.getErrorCode() != null && !jwtEx.getErrorCode().isBlank()) {
        code = jwtEx.getErrorCode();
      }
    }

    APIResponse<Object> body = APIResponse.nok(HttpStatus.UNAUTHORIZED, code, message);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}

