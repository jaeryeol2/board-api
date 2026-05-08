package com.board.api.services.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.board.api.common.base.ResponseApi;
import com.board.api.dtos.response.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonService {
  private final CommonReader commonReader;

  @Async
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return ResponseApi.completedOk(commonReader.getMenuList());
  }

  @Async
  public CompletableFuture<ResponseApi<Menu>> getMenuInfo(String boardTypeCode) {
    return ResponseApi.completedOk(commonReader.getMenuInfo(boardTypeCode));
  }

  @Async
  public CompletableFuture<ResponseApi<Long>> getUserCount() {
    return ResponseApi.completedOk(commonReader.getUserCount());
  }
}
