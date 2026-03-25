package com.project.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.Board;
import com.project.api.dtos.Board.BoardDetail;
import com.project.api.dtos.BoardFilter;
import com.project.api.repositories.read.ReadBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  private final ReadBoardRepository readBoardRepository;

  @Async("appAsync")
  public CompletableFuture<ResponseApi<List<Board>>> getBoardList(final BoardType type,
      final BoardFilter filter) {
    final String boardCode = type.getCode();
    filter.setBoardTypeCode(boardCode);

    return ResponseApi.asyncOk(() -> readBoardRepository.findBoardListByFilter(filter));
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final Long id) {
    return ResponseApi.asyncOk(() -> readBoardRepository.findBoardById(id))
        .exceptionallyAsync(e -> {
          log.error("getBoardDetail error : ", e);
          throw new BusinessException("존재하지 않는 게시물입니다.");
        });
  }
}
