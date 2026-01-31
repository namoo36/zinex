package namoo.zinex.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
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

  /// Object를 JSON으로 직렬화
  @Bean
  public ObjectMapper redisObjectMapper() {
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();

    return new ObjectMapper()
            .registerModule(new JavaTimeModule())   // 날짜 타입 지원
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)  // JSON에 없는 필드는 무시
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)   // 폴리모픽 타입 지원
            .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);   // 날짜를 ISO 포맷으로 저장
  }

  /// Object 용 RedisTemplate
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    // JSON 직렬화/역직렬화
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

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

