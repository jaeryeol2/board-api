package com.board.api.services.common;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.constants.CacheTime;
import com.board.api.dtos.response.Menu;
import com.board.api.repositories.read.ReadMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommonReader {
  private final RedisTemplate<String, String> redisTemplate;
  private final ReadMenuRepository readMenuRepository;

  @Cacheable(cacheNames = "getMenuList")
  public List<Menu> getMenuList() {
    return readMenuRepository.findMenuList();
  }

  // null 캐싱 비활성화 설정이라 결과가 없으면 저장하지 않습니다. (저장 시 예외 발생)
  @Cacheable(cacheNames = "getMenuInfo", key = "#boardTypeCode", unless = "#result == null")
  public Menu getMenuInfo(String boardTypeCode) {
    return readMenuRepository.findMenuByCode(boardTypeCode);
  }

  public Long getUserCount() {
    final String key = CacheTime.GET_TRAFFIC_INFO.getCacheName();
    final double now = System.currentTimeMillis();
    final double windowStart = now - CacheTime.GET_TRAFFIC_INFO.getTtl().toMillis();

    try {
      redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
      final Long count = redisTemplate.opsForZSet().zCard(key);
      return count != null ? count : 0L;
    } catch (Exception e) {
      log.error("getUserCount error : {}", e.getMessage());
      return 0L;
    }
  }

  public void recordUserActivity(String userId) {
    if (userId == null || userId.isBlank()) {
      return;
    }
    final String key = CacheTime.GET_TRAFFIC_INFO.getCacheName();
    final double now = System.currentTimeMillis();
    try {
      redisTemplate.opsForZSet().add(key, userId, now);
    } catch (Exception e) {
      log.error("recordUserActivity error : {}", e.getMessage());
    }
  }

  public void removeUserActivity(String userId) {
    if (userId == null || userId.isBlank()) {
      return;
    }
    final String key = CacheTime.GET_TRAFFIC_INFO.getCacheName();
    try {
      redisTemplate.opsForZSet().remove(key, userId);
    } catch (Exception e) {
      log.error("removeUserActivity error : {}", e.getMessage());
    }
  }

}
