package namoo.zinex.auth.dto;

public record SignInResponse(String accessToken, String refreshToken) {
  public static SignInResponse of(String accessToken, String refreshToken) {
    return new SignInResponse(accessToken, refreshToken);
  }
}

