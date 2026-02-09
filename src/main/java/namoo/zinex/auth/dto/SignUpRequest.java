package namoo.zinex.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotBlank @Size(max = 50) String name
) {}

