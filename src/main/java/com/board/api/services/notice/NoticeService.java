package com.board.api.services.notice;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.dtos.request.NoticeFilter;
import com.board.api.dtos.request.Write.NoticeWrite;
import com.board.api.dtos.response.Notice;
import com.board.api.entities.AppNotice;
import com.board.api.services.user.UserReader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {
  private final UserReader userReader;
  private final NoticeReader noticeReader;
  private final NoticeWriter noticeWriter;

  @Async
  public CompletableFuture<ResponseApi<ResponseList<Notice>>> getNoticeList(NoticeFilter filter) {
    return ResponseApi.completedOk(noticeReader.getNoticeList(filter));
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> writeNotice(NoticeWrite write,
      HttpServletRequest request) {
    final String userId = userReader.getUserId(request);

    return ResponseApi.completedOk(noticeWriter.writeNotice(AppNotice.builder()
        .title(write.getTitle()).content(write.getContent()).writerId(userId).build()));
  }
}
