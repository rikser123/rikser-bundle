package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import rikser123.bundle.service.RedisCacheService;
import rikser123.bundle.service.impl.RedisCacheServiceImpl;

@Configuration
public class RedisConfig {
  @ConditionalOnProperty(name = "bundle.redis.enabled", havingValue = "true")
  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
    var template = new RedisTemplate<String, String>();
    template.setConnectionFactory(factory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.redis.enabled", havingValue = "true")
  public RedisCacheService redisCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
    return new RedisCacheServiceImpl(redisTemplate, objectMapper);
  }
}