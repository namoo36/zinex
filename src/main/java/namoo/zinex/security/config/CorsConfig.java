package namoo.zinex.security.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // 허용 Origin
    config.setAllowedOriginPatterns(List.of("*"));

    // 허용 Method
    config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    ));

    // 허용 Header
    config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Count"
    ));

    // RefreshToken을 쿠키로 운용하는 경우 필요
    config.setAllowCredentials(true);

    // 쿠키 기반 refresh를 쓸 때 Set-Cookie가 내려갈 수 있으니 노출(선택)
    config.setExposedHeaders(List.of(HttpHeaders.SET_COOKIE));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}

