package com.project.api.services.write;

import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.request.BoardWrite;
import com.project.api.dtos.request.CommentWrite;
import com.project.api.dtos.response.ResponseS3;
import com.project.api.dtos.response.User;
import com.project.api.entities.AppBoard;
import com.project.api.entities.AppBoardComment;
import com.project.api.entities.AppBoardFile;
import com.project.api.repositories.write.WriteBoardFileRepository;
import com.project.api.repositories.write.WriteBoardRepository;
import com.project.api.repositories.write.WriteCommentRepository;
import com.project.api.services.S3Service;
import com.project.api.services.read.ReadUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WriteBoardService {
  private final S3Service s3Service;
  private final ReadUserService readUserService;

  private final WriteBoardRepository writeBoardRepository;
  private final WriteCommentRepository writeCommentRepository;
  private final WriteBoardFileRepository writeBoardFileRepository;

  @Transactional
  @CacheEvict(cacheNames = "getBoardList", key = "#boardType.code + '_' + 1")
  public ResponseApi<Boolean> writeBoard(final BoardType boardType, final BoardWrite write,
      final HttpServletRequest request) {
    try {
      final User user = readUserService.getUserInfo(request).join().getData();
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

  @Transactional
  public ResponseApi<Boolean> writeComment(long boardId, CommentWrite write,
      HttpServletRequest request) {
    AppBoardComment comment = writeCommentRepository.save(AppBoardComment.builder()//
        .board(AppBoard.builder()//
            .id(boardId)//
            .build())//
        .content(write.getContent())//
        .parentId(write.getParentId())//
        .writerId(readUserService.getUserId(request)//
            .join()//
            .getData())//
        .build());

    return ResponseApi.ok(HttpStatus.CREATED, "created", comment.getId() > 0L);
  }
}
