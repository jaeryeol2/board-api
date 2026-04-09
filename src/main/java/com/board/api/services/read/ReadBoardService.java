package com.board.api.services.read;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.repositories.read.ReadBoardRepository;
import com.board.api.services.write.WriteBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadBoardService {
  private final WriteBoardService writeBoardService;
  private final ReadBoardRepository readBoardRepository;

  @Async
  public CompletableFuture<ResponseApi<List<?>>> getHomRenderData() {
    return ResponseApi.completedOk(null);
  }

  @Async
  @Cacheable(cacheNames = "getBoardList", key = "#type.code + '_' + #filter.page")
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardList(final BoardType type,
      final BoardFilter filter) {
    final String boardTypeCode = type.getCode();
    filter.setBoardTypeCode(boardTypeCode);
    final long count = readBoardRepository.findBoardCountById(filter);

    return ResponseApi.completedOk(Math.toIntExact(count), CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardByIds(filter);
          return readBoardRepository.findBoardList(boardTypeCode, ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }

  @Async
  @Cacheable(cacheNames = "getBoardList", key = "'board-popular' + '_' + #filter.page")
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardPopularList(
      final BoardFilter filter) {
    final long count = readBoardRepository.findBoardPopularCount();

    return ResponseApi.completedOk(Math.toIntExact(count), CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardPopularByIds(filter);
          return readBoardRepository.findBoardPopularList(ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }


  @Async
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final Long id) {
    Optional<BoardDetail> optional = Optional.ofNullable(readBoardRepository.findBoardById(id));
    BoardDetail detail = null;

    if (optional.isPresent()) {
      detail = optional.get();

      detail.setCommentList(readBoardRepository.findBoardCommentByBoardId(id));
      detail.setFileList(readBoardRepository.findBoardFileByBoardId(id));

      try {
        writeBoardService.updateViewCount(id);
      } catch (Exception e) {
        log.warn("view count fail. : ", e);
      }
    } else {
      throw new BusinessException("존재하지 않는 게시물입니다.");
    }

    return ResponseApi.completedOk(detail);
  }

  @Async
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(long boardId) {
    return ResponseApi.completedOk(readBoardRepository.findBoardCommentByBoardId(boardId));
  }


}
