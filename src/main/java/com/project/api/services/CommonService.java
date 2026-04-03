package com.project.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.project.api.common.base.ResponseApi;
import com.project.api.dtos.response.Menu;
import com.project.api.dtos.response.User;
import com.project.api.repositories.read.ReadMenuRepository;
import com.project.api.repositories.read.ReadUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonService {
  private final ReadMenuRepository readMenuRepository;
  private final ReadUserRepository readUserRepository;

  @Async("appAsync")
  @Cacheable(cacheNames = "getMenuList")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return ResponseApi.asyncOk(readMenuRepository::findMenuList);
  }

  @Async("appAsync")
  @Cacheable(cacheNames = "getUserInfo", key = "#userId")
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return ResponseApi.asyncOk(() -> readUserRepository.findUserById(userId));
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<Menu>> getMenuInfo(String boardTypeCode) {
    return ResponseApi.asyncOk(() -> readMenuRepository.findMenuByCode(boardTypeCode));
  }
}
