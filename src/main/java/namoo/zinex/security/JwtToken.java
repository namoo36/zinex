package namoo.zinex.security;

/**
 * JWT 발급 결과 DTO.
 *
 * @param accessToken  API 호출용 Access Token
 * @param refreshToken Access 재발급용 Refresh Token
 */
public record JwtToken(
    String accessToken,
    String refreshToken
) {
  public static JwtToken of(String accessToken, String refreshToken) {
    return new JwtToken(accessToken, refreshToken);
  }
}
