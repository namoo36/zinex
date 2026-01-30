package namoo.zinex.security;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

@EnableConfigurationProperties(JwtProperties.class)
@Configuration
public class SecurityConfig {

  private static final AntPathRequestMatcher[] SWAGGER_WHITELIST =
      new AntPathRequestMatcher[] {
        AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
        AntPathRequestMatcher.antMatcher("/swagger-ui.html")
      };

  private static final AntPathRequestMatcher[] PUBLIC_WHITELIST =
      new AntPathRequestMatcher[] {
        // Health check
        AntPathRequestMatcher.antMatcher("/actuator/health/**"),
        // Auth endpoints (추후 구현)
        AntPathRequestMatcher.antMatcher("/api/auth/**")
      };

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SWAGGER_WHITELIST).permitAll()
            .requestMatchers(PUBLIC_WHITELIST).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProperties props) {
    if (!StringUtils.hasText(props.secret())) {
      throw new JwtException("jwt.secret is required (Base64-encoded HS256 key)");
    }

    byte[] secretBytes;
    try {
      secretBytes = Base64.getDecoder().decode(props.secret());
    } catch (IllegalArgumentException e) {
      throw new JwtException("jwt.secret must be Base64-encoded", e);
    }
    SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }
}

