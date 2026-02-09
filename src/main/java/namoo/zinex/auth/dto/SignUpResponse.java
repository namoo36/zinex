package namoo.zinex.auth.dto;

public record SignUpResponse(Long userId, String email, String name) {
  public static SignUpResponse of(Long userId, String email, String name) {
    return new SignUpResponse(userId, email, name);
  }
}

