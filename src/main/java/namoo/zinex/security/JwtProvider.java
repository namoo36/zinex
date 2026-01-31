package namoo.zinex.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import namoo.zinex.user.domain.User;
import namoo.zinex.user.enums.Role;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static namoo.zinex.core.config.GlobalVariables.NAME_CLAIM_KEY;
import static namoo.zinex.core.config.GlobalVariables.ROLE_CLAIM_KEY;

@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-ttl}")
    private long ACCESS_EXPIRATION_TIME; // AccessToken 만료시간

    @Value("${jwt.refresh-ttl}")
    private long REFRESH_EXPIRATION_TIME; // RefreshToken 만료시간

    private SecretKey secretKey;

    /// SecretKey 생성
    @PostConstruct
    public void initializeSecretKey(){
        byte[] bytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        secretKey = Keys.hmacShaKeyFor(bytes);
    }

    /// AccessToken 생성
    public String createAccessToken(Long userId, String userName, Role role) {
        long now = System.currentTimeMillis();
        Date accessExpireTime = new Date(now + ACCESS_EXPIRATION_TIME);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(ROLE_CLAIM_KEY, role)
                .claim(NAME_CLAIM_KEY, userName)
                .issuedAt(new Date(now))
                .expiration(accessExpireTime)
                .signWith(secretKey)
                .compact();
    }

    /// RefreshToken 생성
    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        Date refreshExpireTime = new Date(now + REFRESH_EXPIRATION_TIME);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(refreshExpireTime)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private static Authentication getAuthentication() {

    }
}
