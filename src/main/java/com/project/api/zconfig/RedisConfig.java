package com.project.api.zconfig;

import java.io.IOException;
import java.time.Duration;
import org.hibernate.type.SerializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.constants.CacheTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!local")
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig implements CachingConfigurer {
  @Value("${spring.data.redis.host}")
  private String host;
  @Value("${spring.data.redis.port}")
  private int port;

  private final ObjectMapper objectMapper;

  @Bean
  RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  CacheManager cacheManager(ResourceLoader resourceLoader, LettuceConnectionFactory factory) {
    final RedisCacheManagerBuilder builder =
        RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(factory);
    final RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new JsonRedisSerializer()))
        .entryTtl(Duration.ofMinutes(5L));

    builder.cacheDefaults(configuration);

    for (CacheTime info : CacheTime.values()) {
      builder.withCacheConfiguration(info.getCacheName(), configuration.entryTtl(info.getTtl()));
    }

    return builder.build();
  }

  private class JsonRedisSerializer implements RedisSerializer<Object> {

    private JsonRedisSerializer() {}

    @Override
    public byte[] serialize(Object object) {
      try {
        return objectMapper.writeValueAsBytes(object);
      } catch (JsonProcessingException e) {
        throw new SerializationException(e.getMessage(), e);
      }
    }

    @Override
    public Object deserialize(byte[] bytes) {
      if (bytes == null) {
        return null;
      }

      try {
        return objectMapper.readValue(bytes, Object.class);
      } catch (IOException e) {
        throw new SerializationException(e.getMessage(), e);
      }
    }

  }

  private static class RelaxedCacheErrorHandler extends SimpleCacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {

      if (log.isErrorEnabled()) {
        log.error("handleCacheGetError error : {}", exception.getMessage());
      }
    }
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new RelaxedCacheErrorHandler();
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate() {
    final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    final JsonRedisSerializer jsonRedisSerializer = new JsonRedisSerializer();

    redisTemplate.setConnectionFactory(redisConnectionFactory());

    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(jsonRedisSerializer);

    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(jsonRedisSerializer);

    return redisTemplate;
  }
}
