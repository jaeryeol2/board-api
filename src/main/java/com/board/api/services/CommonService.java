package com.board.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.board.api.common.base.ResponseApi;
import com.board.api.constants.CacheTime;
import com.board.api.dtos.response.Menu;
import com.board.api.repositories.read.ReadMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonService {
  private final RedisTemplate<String, String> redisTemplate;
  private final ReadMenuRepository readMenuRepository;

  @Async
  @Cacheable(cacheNames = "getMenuList")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return ResponseApi.completedOk(readMenuRepository.findMenuList());
  }

  @Async
  @Cacheable(cacheNames = "getMenuInfo", key = "#boardTypeCode")
  public CompletableFuture<ResponseApi<Menu>> getMenuInfo(String boardTypeCode) {
    return ResponseApi.completedOk(readMenuRepository.findMenuByCode(boardTypeCode));
  }

  @Async
  public CompletableFuture<ResponseApi<Long>> getUserCount() {
    final Long total =
        Stream.of(CacheTime.GET_GUEST_ID.getCacheName(), CacheTime.GET_USER_INFO.getCacheName())
            .mapToLong(cacheName -> {
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

    return ResponseApi.completedOk(total);
  }
}
