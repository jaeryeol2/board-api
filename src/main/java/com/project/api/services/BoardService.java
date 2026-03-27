package com.project.api.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.Board;
import com.project.api.dtos.Board.BoardDetail;
import com.project.api.dtos.Board.BoardFile;
import com.project.api.dtos.BoardFilter;
import com.project.api.dtos.BoardWrite;
import com.project.api.dtos.User;
import com.project.api.repositories.read.ReadBoardRepository;
import com.project.api.repositories.write.WriteBoardRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  @Value("${spring.cloud.aws.s3.endpoint}")
  private String s3Url;
  private final S3Service s3Service;
  private final UserService userService;
  private final ReadBoardRepository readBoardRepository;
  private final WriteBoardRepository writeBoardRepository;

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

  @Transactional
  public ResponseApi<Boolean> writeBoard(final BoardType boardType, final BoardWrite write,
      final HttpServletRequest request) {
    try {
      write.setBoardTypeCode(boardType.getCode());
      User user = userService.getUserInfo(request).join().getData();
      write.setWriterId(user.getUserId());
      final long count = writeBoardRepository.writeBoard(write);

      if (count > 0 && !ObjectUtils.isEmpty(write.getFileList())
          && !write.getFileList().isEmpty()) {
        List<MultipartFile> fileList = write.getFileList();
        final String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (MultipartFile file : fileList) {
          final String key = now.concat("/").concat(UUID.randomUUID().toString());
          final String originName = file.getOriginalFilename();
          final String fileName = key;
          final String fileUrl = s3Url.concat("/board-image/").concat(key);
          final String fileType = "image";
          final long fileSize = file.getSize();
          s3Service.uploadImage(key, file.getInputStream());
          final BoardFile boardFile =
              new BoardFile(0, originName, fileName, fileUrl, fileType, fileSize, null);
          final long fileInserCount = writeBoardRepository.insertBoardFile(boardFile);

          if (fileInserCount <= 0) {
            log.warn("file insert fail... file info : {}", boardFile);
          }
        }

      }
    } catch (Exception e) {
      log.error("writeBoard error : ", e);
      throw new BusinessException("게시글 저장에 실패했습니다.");
    }

    return ResponseApi.ok(true);

  }
}
