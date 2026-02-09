package namoo.zinex.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import namoo.zinex.security.exception.JwtAuthenticationException;
import namoo.zinex.user.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static namoo.zinex.core.config.GlobalVariables.*;
import static namoo.zinex.core.config.GlobalVariables.REFRESH_TOKEN_COOKIE_NAME;
import static namoo.zinex.core.config.GlobalVariables.REFRESH_TOKEN_COOKIE_PATH;
import static namoo.zinex.core.config.GlobalVariables.ROLE_CLAIM_KEY;

@Slf4j
@Component
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-ttl}")
    private Duration ACCESS_EXPIRATION_TIME; // AccessToken 만료시간

    @Value("${jwt.refresh-ttl}")
    private Duration REFRESH_EXPIRATION_TIME; // RefreshToken 만료시간

    private SecretKey secretKey;

    /// SecretKey 생성
    @PostConstruct
    public void initializeSecretKey(){
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /// AccessToken 생성
    public String createAccessToken(Long userId, String userName, Role role) {
        long now = System.currentTimeMillis();
        Date accessExpireTime = new Date(now + ACCESS_EXPIRATION_TIME.toMillis());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti) // 표준 클레임: jti (AccessToken blacklist용)
                .subject(userId.toString())
                .claim(ROLE_CLAIM_KEY, role)
                .claim(NAME_CLAIM_KEY, userName)
                .issuedAt(new Date(now))
                .expiration(accessExpireTime)
                .signWith(secretKey)
                .compact();
    }

    /// RefreshToken 생성 (호환용: sessionId/jti 없이 발급됨)
    /// 동시 로그인 + rotate/session 방식을 쓰려면 createRefreshToken(userId, sessionId)를 사용하세요.
    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        Date refreshExpireTime = new Date(now + REFRESH_EXPIRATION_TIME.toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(refreshExpireTime)
                .signWith(secretKey)
                .compact();
    }

    /**
     * RefreshToken 생성 (rotate + family 지원)
     *
     * <p>- sub: userId
     * <p>- sessionId: 세션 식별자
     * <p>- jti: 토큰(1장) 식별자 (rotation 시 매번 변경)
     */
    public String createRefreshToken(Long userId, String sessionId) {
        long now = System.currentTimeMillis();
        Date refreshExpireTime = new Date(now + REFRESH_EXPIRATION_TIME.toMillis());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti) // 표준 클레임: jti
                .subject(userId.toString())
                .claim(SESSION_ID_CLAIM_KEY, sessionId)
                .issuedAt(new Date(now))
                .expiration(refreshExpireTime)
                .signWith(secretKey)
                .compact();
    }

    ///  RefreshToken 쿠키 저장
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("strict")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(REFRESH_EXPIRATION_TIME)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /// RefreshToken 쿠키에서 삭제(maxAge = 0으로 내려주는 방식)
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("strict")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /// 토큰 유효성 검사
    public void validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SecurityException | MalformedJwtException e) {
            throw new JwtAuthenticationException("INVALID_TOKEN", "Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("EXPIRED_TOKEN", "Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("UNSUPPORTED_TOKEN", "Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("EMPTY_CLAIMS", "JWT claims string is empty.", e);
        }
    }

    /**
     * 토큰에서 Claims를 추출합니다(서명 검증 포함).
     *
     * <p>validateToken()과 달리, 성공 시 Claims를 반환합니다.
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(Claims claims) {
        String sub = claims.getSubject();
        return sub != null ? Long.parseLong(sub) : null;
    }

    public Role getRoleFromToken(Claims claims) {
        Object raw = claims.get(ROLE_CLAIM_KEY);
        if (raw == null) return null;
        if (raw instanceof Role r) return r;
        return Role.valueOf(raw.toString());
    }

    public String getJtiFromToken(Claims claims) {
        return claims != null ? claims.getId() : null;
    }

    public long getExpirationMillisFromToken(Claims claims) {
        if (claims == null || claims.getExpiration() == null) return 0L;
        return claims.getExpiration().getTime();
    }
}