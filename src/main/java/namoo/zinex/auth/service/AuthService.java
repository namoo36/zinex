package namoo.zinex.auth.service;

import namoo.zinex.auth.dto.SignUpRequest;
import namoo.zinex.auth.dto.SignUpResponse;
import namoo.zinex.security.dto.JwtToken;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

  SignUpResponse signup(SignUpRequest request);

  JwtToken login(String email, String rawPassword);

  JwtToken refresh(String refreshToken);

  void logout(String refreshToken, String accessToken);
}

