package com.board.api.services.notice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.dtos.request.NoticeFilter;
import com.board.api.dtos.response.Notice;
import com.board.api.repositories.read.ReadNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {
  private final NoticeWriter noticeWriter;
  private final ReadNoticeRepository readNoticeRepository;

  @Async
  public CompletableFuture<ResponseApi<ResponseList<Notice>>> getNoticeList(NoticeFilter filter) {
    final long count = readNoticeRepository.findNoticeCount();

    List<Notice> notices = CaseGet.when(count > 0)//
        .then(Optional.ofNullable(readNoticeRepository.findNotices(filter))//
            .filter(itmes -> !itmes.isEmpty())//
            .orElseGet(ArrayList::new))//
        .get();

    return ResponseApi.completedOk(((int) count), notices);
  }
}
