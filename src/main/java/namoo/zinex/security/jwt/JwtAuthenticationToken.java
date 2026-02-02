package namoo.zinex.security.jwt;

import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import namoo.zinex.user.enums.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/// Authentication을 JWT 토큰 기반으로 표현하기 위한 커스텀 클래스
@Getter
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

  private final Claims claims;  // JWT 토큰에 담긴 클레임(userId, role, claims) 저장

  /// 인증 전 생성자
  private JwtAuthenticationToken(Object principal, Object credentials) {
    super(principal, credentials);
    this.claims = null;
  }

  /// 인증 후 생성자
  private JwtAuthenticationToken(
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities,
      Claims claims
  ) {
    super(principal, credentials, authorities);
    this.claims = claims;
  }

  /// 인증 전 토큰(AccessToken만 담은 상태)
  public static JwtAuthenticationToken beforeOf(String accessToken) {
    return new JwtAuthenticationToken(accessToken, "");
  }

  /// 인증 후 토큰(userId + role + claims)
  public static JwtAuthenticationToken afterOf(long userId, Role role, Claims claims) {
    return new JwtAuthenticationToken(userId, "", toAuthorities(role), claims);
  }

  /// AccessToken 반환 (before 토큰에서 사용)
  public String getAccessToken() {
    Object principal = getPrincipal();
    return principal != null ? principal.toString() : null;
  }

  private static List<GrantedAuthority> toAuthorities(Role role) {
    // Spring Security 관례: "ROLE_" prefix
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }
}

