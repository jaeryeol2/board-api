package com.board.api.services.read;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.board.api.common.base.ResponseApi;
import com.board.api.dtos.response.Menu;
import com.board.api.repositories.read.ReadMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonService {
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
}
