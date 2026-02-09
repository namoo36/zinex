package namoo.zinex.auth.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthRepository {

  private final StringRedisTemplate stringRedisTemplate;

  private static final String REFRESH_KEY_PREFIX = "auth:refresh:"; // auth:refresh:{userId}:{sessionId}

  public void saveRefreshToken(Long userId, String sessionId, String refreshToken, Duration ttl) {
    stringRedisTemplate.opsForValue().set(refreshKey(userId, sessionId), refreshToken, ttl);
  }

  public String getRefreshToken(Long userId, String sessionId) {
    return stringRedisTemplate.opsForValue().get(refreshKey(userId, sessionId));
  }

  public void deleteRefreshToken(Long userId, String sessionId) {
    stringRedisTemplate.delete(refreshKey(userId, sessionId));
  }

  private String refreshKey(Long userId, String sessionId) {
    return REFRESH_KEY_PREFIX + userId + ":" + sessionId;
  }
}

