package com.project.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.project.api.common.base.ResponseApi;
import com.project.api.dtos.Menu;
import com.project.api.repositories.read.ReadMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonService {
  private final ReadMenuRepository readMenuRepository;

  @Async("appAsync")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return ResponseApi.asyncOk(readMenuRepository::selectMenuList);
  }
}
