package namoo.zinex.security.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import namoo.zinex.security.exception.CustomAccessDeniedHandler;
import namoo.zinex.security.exception.CustomAuthenticationFailureHandler;
import namoo.zinex.security.exception.CustomAuthenticationEntryPoint;
import namoo.zinex.security.filter.JwtAuthenticationFilter;
import namoo.zinex.security.jwt.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final CustomAuthenticationFailureHandler authenticationFailureHandler;    // 인증 실패 시 401 응답
  private final CorsConfigurationSource corsConfigurationSource;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;           // 인증 자체가 안 된 경우 401
  private final CustomAccessDeniedHandler accessDeniedHandler;                     // 권한 부족 403

  @Bean
  public AuthenticationManager authenticationManager() {
    // JwtAuthenticationFilter가 JwtAuthenticationToken을 authenticate할 때 사용
    return new ProviderManager(List.of(jwtAuthenticationProvider));
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            // role 기반 인가(주식거래소 운영/관리 기능 대비) -> 인증 + ROLE_ADMIN 필요
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // /api/**는 인증 필수
            .requestMatchers("/api/**").authenticated()
            // 그 외는 공개(문서/헬스체크 등)
            .anyRequest().permitAll()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.authenticationManager(authenticationManager()).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AccessToken 인증 필터.
   *
   * <p>/api/** 요청에 대해 JWT 인증을 시도하되, 공개 경로(/api/auth/**, swagger, health)는 제외합니다.
   */
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    // JWT 적용 대상
    RequestMatcher apiMatcher = PathPatternRequestMatcher.withDefaults().matcher("/api/**");

    // 배제 대상
    RequestMatcher exclude =
        new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher("/api/auth/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui.html"),
            PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/actuator/health/**")
        );

    RequestMatcher jwtAuthMatcher = new AndRequestMatcher(apiMatcher, new NegatedRequestMatcher(exclude));

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtAuthMatcher, authenticationFailureHandler);
    filter.setAuthenticationManager(authenticationManager());
    filter.setAuthenticationFailureHandler(authenticationFailureHandler);
    return filter;
  }
}

