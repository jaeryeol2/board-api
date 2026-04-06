package com.project.api.services.read;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.base.ResponseApi.ResponseList;
import com.project.api.common.condition.CaseGet;
import com.project.api.constants.BoardType;
import com.project.api.dtos.request.BoardFilter;
import com.project.api.dtos.response.Board;
import com.project.api.dtos.response.Board.BoardComment;
import com.project.api.dtos.response.Board.BoardDetail;
import com.project.api.repositories.read.ReadBoardRepository;
import com.project.api.repositories.write.WriteBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadBoardService {
  private final TransactionTemplate transaction;
  private final ReadBoardRepository readBoardRepository;
  private final WriteBoardRepository writeBoardRepository;

  @Async("appAsync")
  public CompletableFuture<ResponseApi<List<?>>> getHomRenderData() {
    return ResponseApi.asyncOk(() -> null);
  }

  @Async("appAsync")
  @Cacheable(cacheNames = "getBoardList", key = "#type.code + '_' + #filter.page")
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardList(final BoardType type,
      final BoardFilter filter) {
    final String boardTypeCode = type.getCode();
    filter.setBoardTypeCode(boardTypeCode);
    final long count = readBoardRepository.findBoardCountById(filter);

    return ResponseApi.asyncOk(Math.toIntExact(count), () -> CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardByIds(filter);
          return readBoardRepository.findBoardList(boardTypeCode, ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }

  @Async("appAsync")
  @Cacheable(cacheNames = "getBoardList", key = "'board-popular' + '_' + #filter.page")
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardPopularList(
      final BoardFilter filter) {
    final long count = readBoardRepository.findBoardPopularCount();

    return ResponseApi.asyncOk(Math.toIntExact(count), () -> CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardPopularByIds(filter);
          return readBoardRepository.findBoardPopularList(ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }


  @Async("appAsync")
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final Long id) {
    return ResponseApi.asyncOk(() -> {
      Optional<BoardDetail> optional = Optional.ofNullable(readBoardRepository.findBoardById(id));
      BoardDetail detail = null;

      if (optional.isPresent()) {
        detail = optional.get();

        detail.setCommentList(readBoardRepository.findBoardCommentByBoardId(id));
        detail.setFileList(readBoardRepository.findBoardFileByBoardId(id));

        transaction.execute(status -> {
          writeBoardRepository.updateViewCount(id);
          return status;
        });
      }

      return detail;
    }).exceptionallyAsync(e -> {
      log.error("getBoardDetail error : ", e);
      throw new BusinessException("존재하지 않는 게시물입니다.");
    });
  }



  @Async("appAsync")
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(long boardId) {
    return ResponseApi.asyncOk(() -> readBoardRepository.findBoardCommentByBoardId(boardId));
  }


}
