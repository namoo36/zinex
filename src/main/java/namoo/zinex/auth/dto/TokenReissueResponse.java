package namoo.zinex.auth.dto;

public record TokenReissueResponse(String accessToken) {
  public static TokenReissueResponse of(String accessToken) {
    return new TokenReissueResponse(accessToken);
  }
}

