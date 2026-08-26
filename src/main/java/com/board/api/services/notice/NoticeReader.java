package com.board.api.services.notice;

import java.util.Collections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class NoticeReader {
  private final ReadNoticeRepository readNoticeRepository;

  public ResponseList<Notice> getNoticeList(NoticeFilter filter) {
    long count = readNoticeRepository.findNoticeCount();

    return ResponseList.of(Math.toIntExact(count),
        CaseGet.when(() -> count > 0).then(() -> readNoticeRepository.findNotices(filter))//
            .other(Collections::emptyList)//
            .get());
  }
}
