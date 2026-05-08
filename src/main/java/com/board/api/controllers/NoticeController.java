package com.board.api.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.dtos.request.NoticeFilter;
import com.board.api.dtos.response.Notice;
import com.board.api.services.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
  private final NoticeService noticeService;


  @GetMapping("")
  public CompletableFuture<ResponseApi<ResponseList<Notice>>> getNoticeList(NoticeFilter filter) {
    return noticeService.getNoticeList(filter);
  }
}
