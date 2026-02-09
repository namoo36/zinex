package namoo.zinex.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * AccessToken 블랙리스트 관리(로그아웃 시 즉시 무효화 목적).
 *
 * <p>키: auth:blacklist:access:{jti}
 * <p>값: "1"
 * <p>TTL: accessToken 만료까지 남은 시간
 */
@Component
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

  private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";

  private final StringRedisTemplate stringRedisTemplate;

  public void blacklist(String jti, Duration ttl) {
    if (jti == null || jti.isBlank() || ttl == null || ttl.isZero() || ttl.isNegative()) {
      return;
    }
    stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttl);
  }

  public boolean isBlacklisted(String jti) {
    if (jti == null || jti.isBlank()) return false;
    Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti);
    return Boolean.TRUE.equals(exists);
  }
}

