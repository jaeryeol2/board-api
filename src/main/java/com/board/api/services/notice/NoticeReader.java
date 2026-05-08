package com.board.api.services.notice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.repositories.read.ReadNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeReader {
  private final ReadNoticeRepository readNoticeRepository;
}
