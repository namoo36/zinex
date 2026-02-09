package namoo.zinex.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;
  @Value("${spring.data.redis.port}")
  private int port;
  @Value("${spring.data.redis.password}")
  private String password;

  /// Redis 연결 팩토리 설정
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // RedisStandaloneConfiguration -> 단일 Redis 서버 연결용
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();

    redisConfig.setHostName(host);
    redisConfig.setPort(port);
    redisConfig.setPassword(password);

    // LettuceConnectionFactory -> 비동기/논블로킹 Redis 클라이언트
    return new LettuceConnectionFactory(redisConfig);
  }

  /// 문자열 전용 RedisTemplate(직렬화 설정 불필요)
  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  /// Object 용 RedisTemplate
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    // Redis 전용 ObjectMapper (웹 ObjectMapper에 영향 주지 않도록 Bean으로 등록하지 않음)
    ObjectMapper redisMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // JSON 직렬화/역직렬화
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisMapper);

    RedisTemplate<String, Object> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer()); // Key 문자열 저장
    template.setValueSerializer(jsonSerializer);  // 객체를 JSON 문자열로 변환

    template.setHashKeySerializer(new StringRedisSerializer()); // Key 문자열 저장
    template.setHashValueSerializer(jsonSerializer);  // 객체를 JSON 문자열로 변환

    template.afterPropertiesSet();  // RedisTemplate 설정 끝
    return template;
  }
}

