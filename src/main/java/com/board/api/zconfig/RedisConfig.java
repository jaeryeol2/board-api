package com.board.api.zconfig;

import java.time.Duration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
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

  /**
   * 캐시(Redis) 오류가 인증·쓰기 등 본 기능 장애로 번지지 않도록 모든 캐시 오류를 로깅 후 무시합니다.
   * (조회 실패 시 원본 메서드 실행, 저장·삭제 실패 시 캐시만 건너뜀)
   */
  private static class RelaxedCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
      log.error("handleCacheGetError cache : {}, error : {}", cache.getName(),
          exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key,
        Object value) {
      log.error("handleCachePutError cache : {}, error : {}", cache.getName(),
          exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
      log.error("handleCacheEvictError cache : {}, error : {}", cache.getName(),
          exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
      log.error("handleCacheClearError cache : {}, error : {}", cache.getName(),
          exception.getMessage());
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
