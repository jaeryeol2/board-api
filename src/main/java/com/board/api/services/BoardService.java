package com.board.api.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.request.BoardWrite;
import com.board.api.dtos.request.CommentWrite;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.dtos.response.ResponseS3;
import com.board.api.dtos.response.User;
import com.board.api.entities.AppBoard;
import com.board.api.entities.AppBoardComment;
import com.board.api.entities.AppBoardFile;
import com.board.api.repositories.read.ReadBoardRepository;
import com.board.api.repositories.write.WriteBoardFileRepository;
import com.board.api.repositories.write.WriteBoardRepository;
import com.board.api.repositories.write.WriteCommentRepository;
import com.board.api.services.internal.InUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  private final S3Service s3Service;
  private final InUserService inUserService;
  private final TransactionTemplate writeTransaction;
  private final ReadBoardRepository readBoardRepository;
  private final WriteBoardRepository writeBoardRepository;
  private final WriteCommentRepository writeCommentRepository;
  private final WriteBoardFileRepository writeBoardFileRepository;

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
        writeTransaction.execute(status -> writeBoardRepository.updateViewCount(id));
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

  @Transactional
  @CacheEvict(cacheNames = "getBoardList", key = "#boardType.code + '_' + 1")
  public ResponseApi<Boolean> writeBoard(final BoardType boardType, final BoardWrite write,
      final HttpServletRequest request) {
    try {
      final String userId = inUserService.getUserId(request);
      final User user = inUserService.getUserInfo(userId);
      write.setBoardTypeCode(boardType.getCode());
      write.setWriterId(user.getUserId());

      final AppBoard appBoard = writeBoardRepository.save(AppBoard.builder()//
          .boardTypeCode(write.getBoardTypeCode())//
          .title(write.getTitle())//
          .content(write.getContent())//
          .writerId(write.getWriterId())//
          .build());

      if (appBoard.getId() > 0 && !ObjectUtils.isEmpty(write.getAttachments())
          && !write.getAttachments().isEmpty()) {
        final List<MultipartFile> fileList = write.getAttachments();

        for (MultipartFile file : fileList) {
          final ResponseS3 s3 = Optional.ofNullable(s3Service.uploadImage(file))//
              .orElseThrow(() -> {
                throw new BusinessException("파일업로드에 실패했습니다.");
              });
          final String originName = file.getOriginalFilename();
          final String extension = ".".concat(FilenameUtils.getExtension(originName));
          final String fileName = s3.getFileName().concat(extension);
          final String fileType = "image";
          final long fileSize = file.getSize();
          final AppBoardFile appBoardFile = writeBoardFileRepository.save(
              AppBoardFile.builder().board(appBoard).originalFileName(originName).fileName(fileName)
                  .fileUrl(s3.getUrl()).fileType(fileType).fileSize(fileSize).build());

          if (appBoardFile.getId() <= 0) {
            log.warn("file insert fail... file info : {}", appBoardFile);
          }
        }

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

  @Transactional
  public ResponseApi<Boolean> writeComment(long boardId, CommentWrite write,
      HttpServletRequest request) {
    AppBoardComment comment = writeCommentRepository.save(AppBoardComment.builder()//
        .board(AppBoard.builder()//
            .id(boardId)//
            .build())//
        .content(write.getContent())//
        .parentId(write.getParentId())//
        .writerId(inUserService.getUserId(request))//
        .build());

    return ResponseApi.ok(HttpStatus.CREATED, "created", comment.getId() > 0L);
  }


}
