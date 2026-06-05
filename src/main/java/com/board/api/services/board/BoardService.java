package com.board.api.services.board;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.request.Write.BoardWrite;
import com.board.api.dtos.request.Write.CommentWrite;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.dtos.response.Home;
import com.board.api.dtos.response.User;
import com.board.api.entities.AppBoard;
import com.board.api.entities.AppBoardComment;
import com.board.api.services.user.UserReader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  private final UserReader userReader;
  private final BoardReader boardReader;
  private final BoardWriter boardWriter;
  private final TaskExecutor appAsync;

  @Async
  public CompletableFuture<ResponseApi<List<Home>>> getHomeBoardData() {
    return ResponseApi.completedOk(boardReader.getHomeBoardData());
  }

  @Async
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardList(final BoardType type,
      final BoardFilter filter) {
    return ResponseApi.completedOk(boardReader.getBoardList(type, filter));
  }

  @Async
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardPopularList(
      final BoardFilter filter) {
    return ResponseApi.completedOk(boardReader.getBoardPopularList(filter));
  }


  @Async
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final Long id) {
    final BoardDetail detail = boardReader.getBoardDetail(id);

    if (!ObjectUtils.isEmpty(detail)) {
      CompletableFuture.runAsync(() -> boardWriter.updateViewCount(id), appAsync);
    }

    return ResponseApi.completedOk(detail);
  }

  @Async
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(long boardId) {
    return ResponseApi.completedOk(boardReader.getCommentList(boardId));
  }

  @Transactional
  public ResponseApi<Boolean> writeBoard(BoardType boardType, BoardWrite write,
      HttpServletRequest request) {
    try {
      final String userId = userReader.getUserId(request);
      final User user = userReader.getUserInfo(userId);

      write.setBoardTypeCode(boardType.getCode());
      write.setWriterId(user.getUserId());

      final AppBoard appBoard = boardWriter.writeBoard(AppBoard.builder()//
          .boardTypeCode(write.getBoardTypeCode())//
          .title(write.getTitle())//
          .content(write.getContent())//
          .writerId(write.getWriterId())//
          .build());

      if (appBoard.getId() > 0 && !ObjectUtils.isEmpty(write.getAttachments())
          && !write.getAttachments().isEmpty()) {
        final List<MultipartFile> fileList = write.getAttachments();

        boardWriter.writeBoardFile(appBoard, fileList);
      }
    } catch (Exception e) {
      log.error("writeBoard error : ", e);

      if (e instanceof BusinessException) {
        throw new BusinessException(e.getMessage());
      }

      throw new BusinessException("게시글 저장에 실패했습니다.");
    }

    return ResponseApi.ok(HttpStatus.CREATED, "created", true);

  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> writeComment(long boardId, CommentWrite write,
      HttpServletRequest request) {
    return ResponseApi.completedOk(HttpStatus.CREATED, "created",
        boardWriter.writeComment(AppBoardComment.builder()//
            .board(AppBoard.builder()//
                .id(boardId)//
                .build())//
            .content(write.getContent())//
            .parentId(write.getParentId())//
            .writerId(userReader.getUserId(request))//
            .build()));
  }

  @Transactional
  public ResponseApi<Boolean> modifyBoard(BoardType boardType, long id, BoardWrite write,
      HttpServletRequest request) {
    try {
      final String userId = userReader.getUserId(request);
      final BoardDetail detail = boardReader.getBoardDetail(id);

      if (detail == null) {
        throw new BusinessException("존재하지 않는 게시글입니다.");
      }

      if (!detail.getWriterId().equals(userId)) {
        throw new BusinessException("수정 권한이 없습니다.");
      }
      AppBoard appBoard = AppBoard.builder()//
          .id(id)//
          .title(write.getTitle())//
          .content(write.getContent())//
          .updatedAt(LocalDateTime.now())//
          .build();

      boardWriter.modifyBoard(appBoard);

      if (appBoard.getId() > 0 && !ObjectUtils.isEmpty(write.getAttachments())
          && !write.getAttachments().isEmpty()) {
        final List<MultipartFile> fileList = write.getAttachments();

        boardWriter.writeBoardFile(appBoard, fileList);
      }
    } catch (Exception e) {
      log.error("modifyBoard error : ", e);

      if (e instanceof BusinessException) {
        throw new BusinessException(e.getMessage());
      }

      throw new BusinessException("게시글 수정에 실패했습니다.");
    }

    return ResponseApi.ok(HttpStatus.OK, "updated", true);
  }
}
