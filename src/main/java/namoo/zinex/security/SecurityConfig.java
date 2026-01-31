package namoo.zinex.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(JwtToken.class)
@Configuration
public class SecurityConfig {

}

