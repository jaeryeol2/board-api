package com.board.api.services.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.board.api.common.utils.HtmlSanitizer;
import com.board.api.common.utils.RequestUtil;
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
import com.board.api.dtos.response.ResponseS3;
import com.board.api.services.board.BoardWriter.UploadedFile;
import com.board.api.services.common.RequestGuard;
import com.board.api.services.common.S3Service;
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
  private final S3Service s3Service;
  private final RequestGuard requestGuard;
  private final TaskExecutor appAsync;

  private static final String NOT_FOUND_BOARD = "존재하지 않는 게시물입니다.";

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
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final BoardType boardType,
      final Long id, final HttpServletRequest request) {
    final BoardDetail detail = boardReader.getBoardDetail(id);

    // 경로의 게시판 타입과 글의 실제 타입이 다르면 조회하지 않습니다. (비로그인 허용 경로를 통한 타 게시판 조회 차단)
    // 인기 게시판은 여러 게시판의 글을 모아 보여주므로 제외합니다.
    if (boardType != BoardType.BOARD_POPULAR
        && !boardType.getCode().equals(detail.getBoardTypeCode())) {
      throw new BusinessException(NOT_FOUND_BOARD, HttpStatus.NOT_FOUND);
    }

    final String userId = userReader.getUserId(request);
    // ponytail: 캐시(Redis)가 조회마다 새 객체로 역직렬화하므로 요청별 author 설정이 안전합니다.
    // 로컬(인메모리) 캐시로 바꾸면 객체가 공유되어 사용자 간 값이 섞이므로 그때는 복사본에 설정해야 합니다.
    detail.setAuthor(userId != null && userId.equals(boardReader.getBoardWriterId(id)));

    // 같은 사용자(비로그인은 게스트 ID, 없으면 IP)의 반복 조회는 일정 시간 1회만 조회수에 반영합니다.
    final String viewerId = Objects.requireNonNullElseGet(userId,
        () -> Objects.requireNonNullElseGet(RequestUtil.getGuestId(request),
            () -> RequestUtil.getClientIp(request)));
    if (requestGuard.markViewed(id, viewerId)) {
      CompletableFuture.runAsync(() -> boardWriter.updateViewCount(id), appAsync);
    }

    return ResponseApi.completedOk(detail);
  }

  @Async
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(long boardId) {
    return ResponseApi.completedOk(boardReader.getCommentList(boardId));
  }

  /**
   * 게시글 작성.
   * 첨부 파일 S3 업로드를 DB 작업보다 먼저 수행합니다. DataSource가 LazyConnectionDataSourceProxy라서 첫 쿼리 전까지는
   * 트랜잭션 안에서도 실제 커넥션을 잡지 않으므로, 업로드 동안 write pool 커넥션을 점유하지 않습니다.
   */
  @Transactional
  public ResponseApi<Boolean> writeBoard(BoardType boardType, BoardWrite write,
      HttpServletRequest request) {
    List<UploadedFile> uploaded = List.of();

    try {
      uploaded = uploadAttachments(write.getAttachments());

      final String userId = userReader.getUserId(request);
      final User user = userReader.getUserInfo(userId);

      write.setBoardTypeCode(boardType.getCode());
      write.setWriterId(user.getUserId());

      final AppBoard appBoard = boardWriter.writeBoard(AppBoard.builder()//
          .boardTypeCode(write.getBoardTypeCode())//
          .title(write.getTitle())//
          .content(HtmlSanitizer.sanitize(write.getContent()))//
          .writerId(write.getWriterId())//
          .build());

      if (appBoard.getId() > 0) {
        boardWriter.writeBoardFile(appBoard, uploaded);
      }
    } catch (Exception e) {
      log.error("writeBoard error : ", e);
      deleteUploadedFiles(uploaded);

      // 하위에서 지정한 HTTP 상태(404·400 등)를 유지하도록 원래 예외를 그대로 던집니다.
      if (e instanceof BusinessException businessException) {
        throw businessException;
      }

      throw new BusinessException("게시글 저장에 실패했습니다.");
    }

    return ResponseApi.ok(HttpStatus.CREATED, "created", true);

  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> writeComment(long boardId, CommentWrite write,
      HttpServletRequest request) {
    if (!boardReader.existsBoard(boardId)) {
      throw new BusinessException(NOT_FOUND_BOARD, HttpStatus.NOT_FOUND);
    }

    if (write.getParentId() != null
        && !boardReader.existsCommentInBoard(write.getParentId(), boardId)) {
      throw new BusinessException("존재하지 않는 댓글입니다.", HttpStatus.BAD_REQUEST);
    }

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

  /**
   * 게시글 수정. 업로드를 먼저 하는 이유는 writeBoard 주석 참고. 권한이 없으면 업로드한 파일을 삭제합니다.
   */
  @Transactional
  public ResponseApi<Boolean> modifyBoard(BoardType boardType, long id, BoardWrite write,
      HttpServletRequest request) {
    List<UploadedFile> uploaded = List.of();

    try {
      uploaded = uploadAttachments(write.getAttachments());

      final String userId = userReader.getUserId(request);
      // 캐시된 상세는 익명게시판 작성자 ID가 가려져 있으므로 DB에서 실제 작성자를 확인합니다.
      final String writerId = boardReader.getBoardWriterId(id);

      if (writerId == null) {
        throw new BusinessException("존재하지 않는 게시글입니다.");
      }

      if (!writerId.equals(userId)) {
        throw new BusinessException("수정 권한이 없습니다.");
      }
      AppBoard appBoard = AppBoard.builder()//
          .id(id)//
          .title(write.getTitle())//
          .content(HtmlSanitizer.sanitize(write.getContent()))//
          .build();

      boardWriter.modifyBoard(appBoard);

      if (appBoard.getId() > 0) {
        boardWriter.writeBoardFile(appBoard, uploaded);
      }
    } catch (Exception e) {
      log.error("modifyBoard error : ", e);
      deleteUploadedFiles(uploaded);

      // 하위에서 지정한 HTTP 상태(404·400 등)를 유지하도록 원래 예외를 그대로 던집니다.
      if (e instanceof BusinessException businessException) {
        throw businessException;
      }

      throw new BusinessException("게시글 수정에 실패했습니다.");
    }

    return ResponseApi.ok(HttpStatus.OK, "updated", true);
  }

  @Async
  public CompletableFuture<ResponseApi<String>> uploadImage(MultipartFile file,
      HttpServletRequest request) {
    try {
      final String userId = userReader.getUserId(request);
      if (ObjectUtils.isEmpty(userId)) {
        throw new BusinessException("로그인이 필요한 서비스입니다.");
      }

      if (file == null || file.isEmpty()) {
        throw new BusinessException("업로드할 파일이 비어있습니다.");
      }

      final ResponseS3 s3 = s3Service.uploadImage(file);
      if (s3 == null || ObjectUtils.isEmpty(s3.getUrl())) {
        throw new BusinessException("이미지 업로드에 실패했습니다.");
      }

      // 삭제 API에서 업로더 본인만 지울 수 있도록 업로더를 기록합니다.
      requestGuard.markImageOwner(s3Service.extractKey(s3.getUrl()), userId);

      return ResponseApi.completedOk(HttpStatus.OK, "uploaded", s3.getUrl());
    } catch (Exception e) {
      log.error("uploadImage error : ", e);
      // 하위에서 지정한 HTTP 상태(404·400 등)를 유지하도록 원래 예외를 그대로 던집니다.
      if (e instanceof BusinessException businessException) {
        throw businessException;
      }
      throw new BusinessException("이미지 업로드 중 오류가 발생했습니다.");
    }
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> deleteImage(String url,
      HttpServletRequest request) {
    try {
      final String userId = userReader.getUserId(request);
      if (ObjectUtils.isEmpty(userId)) {
        throw new BusinessException("로그인이 필요한 서비스입니다.");
      }

      final String key = s3Service.extractKey(url);
      if (ObjectUtils.isEmpty(key)) {
        throw new BusinessException("삭제할 이미지 URL이 올바르지 않습니다.");
      }

      // 본인이 업로드한 이미지(업로드 후 일정 시간 이내)만 삭제할 수 있습니다.
      if (!requestGuard.isImageOwner(key, userId)) {
        throw new BusinessException("이미지 삭제 권한이 없습니다.");
      }

      final boolean deleted = s3Service.deleteImage(url);
      return ResponseApi.completedOk(HttpStatus.OK, "deleted", deleted);
    } catch (Exception e) {
      log.error("deleteImage error : ", e);
      // 하위에서 지정한 HTTP 상태(404·400 등)를 유지하도록 원래 예외를 그대로 던집니다.
      if (e instanceof BusinessException businessException) {
        throw businessException;
      }
      throw new BusinessException("이미지 삭제 중 오류가 발생했습니다.");
    }
  }

  /**
   * 첨부 파일을 S3에 업로드합니다. 중간에 실패하면 이미 올린 파일을 지우고 예외를 던집니다.
   */
  private List<UploadedFile> uploadAttachments(final List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }

    final List<UploadedFile> uploaded = new ArrayList<>();
    try {
      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          continue;
        }

        final ResponseS3 s3 = s3Service.uploadImage(file);
        if (s3 == null) {
          throw new BusinessException("파일업로드에 실패했습니다.");
        }

        uploaded.add(new UploadedFile(file.getOriginalFilename(), file.getSize(), s3));
      }
    } catch (RuntimeException e) {
      deleteUploadedFiles(uploaded);
      throw e;
    }

    return uploaded;
  }

  private void deleteUploadedFiles(final List<UploadedFile> uploaded) {
    uploaded.forEach(file -> s3Service.deleteImage(file.s3().getUrl()));
  }
}
