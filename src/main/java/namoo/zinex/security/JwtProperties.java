package namoo.zinex.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String issuer,
    String secret,
    Duration accessTtl,
    Duration refreshTtl
) {}

