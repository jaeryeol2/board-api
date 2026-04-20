package com.board.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.board.api.common.base.ResponseApi;
import com.board.api.dtos.response.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {


  @Async
  public CompletableFuture<ResponseApi<List<Notice>>> getNoticeList() {
    return ResponseApi.completedOk(null);
  }
}
