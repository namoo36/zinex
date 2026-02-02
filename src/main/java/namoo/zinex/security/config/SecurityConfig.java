package namoo.zinex.security.config;

import namoo.zinex.security.dto.JwtToken;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(JwtToken.class)
@Configuration
public class SecurityConfig {

}

