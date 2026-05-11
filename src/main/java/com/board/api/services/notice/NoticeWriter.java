package com.board.api.services.notice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.entities.AppNotice;
import com.board.api.repositories.write.WriteNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class NoticeWriter {
  private final WriteNoticeRepository writeNoticeRepository;

  public boolean writeNotice(AppNotice appNotice) {
    AppNotice result = writeNoticeRepository.save(appNotice);

    return result.getId() > 0L;
  }

}
