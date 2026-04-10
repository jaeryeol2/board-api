package com.board.api.zconfig;

import java.time.Duration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.board.api.constants.CacheTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig implements CachingConfigurer {
  private final ObjectMapper objectMapper;

  @Bean
  CacheManager cacheManager(ResourceLoader resourceLoader,
      RedisConnectionFactory connectionFactory) {
    final RedisCacheManagerBuilder builder =
        RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory);

    final RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(springCacheJsonSerializer()))
        .entryTtl(Duration.ofMinutes(5L));

    builder.cacheDefaults(configuration);

    for (CacheTime info : CacheTime.values()) {
      builder.withCacheConfiguration(info.getCacheName(), configuration.entryTtl(info.getTtl()));
    }

    return builder.build();
  }

  private GenericJackson2JsonRedisSerializer springCacheJsonSerializer() {
    final ObjectMapper mapper = this.objectMapper.copy();

    PolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    mapper.setTypeFactory(
        mapper.getTypeFactory().withClassLoader(Thread.currentThread().getContextClassLoader()));

    return new GenericJackson2JsonRedisSerializer(mapper);
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
  RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // 기존의 redisConnectionFactory() 메서드 호출 대신, 주입받은 객체를 세팅
    redisTemplate.setConnectionFactory(connectionFactory);

    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(springCacheJsonSerializer());

    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(springCacheJsonSerializer());

    return redisTemplate;
  }
}
