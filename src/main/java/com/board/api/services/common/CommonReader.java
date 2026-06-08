package com.board.api.services.common;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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

  @Cacheable(cacheNames = "getMenuInfo", key = "#boardTypeCode")
  public Menu getMenuInfo(String boardTypeCode) {
    return readMenuRepository.findMenuByCode(boardTypeCode);
  }

  public Long getUserCount() {
    return Stream.of(CacheTime.GET_TRAFIC_INFO.getCacheName()).mapToLong(cacheName -> {
      String pattern = cacheName.concat("::*");
      ScanOptions options = ScanOptions.scanOptions().match(pattern).build();

      return redisTemplate.execute((RedisCallback<Long>) conn -> {
        Long count = 0L;

        try (Cursor<byte[]> cursor = conn.keyCommands().scan(options)) {
          while (cursor.hasNext()) {
            cursor.next();
            count++;
          }
        } catch (Exception e) {
          log.error("getTraficCount error : {}", e.getMessage());

          return 0L;
        }

        return count;
      });
    }).sum();

  }

}
