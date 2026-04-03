package com.project.api.services;

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
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.base.ResponseApi.ResponseList;
import com.project.api.common.condition.CaseGet;
import com.project.api.constants.BoardType;
import com.project.api.dtos.request.BoardFilter;
import com.project.api.dtos.request.BoardWrite;
import com.project.api.dtos.response.Board;
import com.project.api.dtos.response.Board.BoardDetail;
import com.project.api.dtos.response.ResponseS3;
import com.project.api.dtos.response.User;
import com.project.api.entities.AppBoard;
import com.project.api.entities.AppBoardFile;
import com.project.api.repositories.read.ReadBoardRepository;
import com.project.api.repositories.write.WriteBoardFileRepository;
import com.project.api.repositories.write.WriteBoardRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  private final S3Service s3Service;
  private final UserService userService;
  private final TransactionTemplate transaction;
  private final ReadBoardRepository readBoardRepository;
  private final WriteBoardRepository writeBoardRepository;
  private final WriteBoardFileRepository writeBoardFileRepository;

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
        }).other(Collections::emptyList)//
        .get());
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(final Long id) {
    return ResponseApi.asyncOk(() -> {
      Optional<BoardDetail> optional = Optional.ofNullable(readBoardRepository.findBoardById(id));
      BoardDetail detail = null;

      if (optional.isPresent()) {
        detail = optional.get();

        detail.setCommentList(readBoardRepository.findBoardCommentById(0));
        detail.setFileList(readBoardRepository.findBoardFileById(id));

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

  @Transactional
  @CacheEvict(cacheNames = "getBoardList", key = "#boardType.code + '_' + 1")
  public ResponseApi<Boolean> writeBoard(final BoardType boardType, final BoardWrite write,
      final HttpServletRequest request) {
    try {
      final User user = userService.getUserInfo(request).join().getData();
      write.setBoardTypeCode(boardType.getCode());
      write.setWriterId(user.getUserId());

      final AppBoard appBoard = writeBoardRepository
          .save(AppBoard.builder().boardTypeCode(write.getBoardTypeCode()).title(write.getTitle())
              .content(write.getContent()).writerId(write.getWriterId()).build());

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
}
