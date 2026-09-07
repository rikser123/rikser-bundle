package rikser123.bundle.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import rikser123.bundle.service.RedisCacheService;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements RedisCacheService {
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${bundle.redis.TTL:P1d}")
  private String TTL;

  @Value("${bundle.redis.prefix:query}")
  private String searchPrefix;

  @Override
  public <T> void put(String key, T value) {
    put(key, value);
  }

  @Override
  public <T> void put(String key, T value, @Nullable String ttl) {
    try {
      var valueStr = objectMapper.writeValueAsString(value);
      var ttlStr = StringUtils.isNoneEmpty(ttl) ? ttl : TTL;
      redisTemplate.opsForValue().set(searchPrefix + key, valueStr, Duration.parse(ttlStr));
      log.info("in redis {}", searchPrefix + key);
    } catch (Exception e) {
      log.warn("Error saving to Redis: {}", e.getMessage(), e);
    }
  }

  @Override
  public <T> Optional<T> get(String key, Class<T> parsedCLass) {
    var resultStr = redisTemplate.opsForValue().get(searchPrefix + key);

    if (!Objects.isNull(resultStr)) {
      try {
        var parsedValue = objectMapper.readValue(resultStr, parsedCLass);
        return Optional.of(parsedValue);
      } catch (JsonProcessingException e) {
        log.warn("Can not parse value from redis {}", searchPrefix + key);
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(searchPrefix + key);
  }

  @Override
  public boolean contains(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(searchPrefix + key));
  }
}
