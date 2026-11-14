package com.board.api.services.board;

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.BusinessException;
import com.board.api.dtos.response.ResponseS3;
import com.board.api.entities.AppBoard;
import com.board.api.entities.AppBoardComment;
import com.board.api.entities.AppBoardFile;
import com.board.api.repositories.write.WriteBoardFileRepository;
import com.board.api.repositories.write.WriteBoardRepository;
import com.board.api.repositories.write.WriteCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class BoardWriter {
  private final WriteBoardRepository writeBoardRepository;
  private final WriteBoardFileRepository writeBoardFileRepository;
  private final WriteCommentRepository writeCommentRepository;

  @Caching(evict = {@CacheEvict(cacheNames = "getBoardList", allEntries = true),
      @CacheEvict(cacheNames = "getHomeBoardData", allEntries = true)})
  public AppBoard writeBoard(AppBoard appBoard) {
    return writeBoardRepository.save(appBoard);
  }

  public int updateViewCount(long id) {
    return writeBoardRepository.updateViewCount(id);
  }

  /**
   * 트랜잭션 밖에서 S3 업로드를 마친 첨부 파일 정보.
   */
  public record UploadedFile(String originalFileName, long fileSize, ResponseS3 s3) {
  }

  /**
   * 업로드가 끝난 첨부 파일의 메타 정보만 저장합니다. (S3 업로드는 BoardService에서 트랜잭션 DB 작업 전에 수행)
   */
  public void writeBoardFile(AppBoard appBoard, List<UploadedFile> fileList) {
    if (fileList == null || fileList.isEmpty()) {
      return;
    }

    final List<AppBoardFile> insertFileList = fileList.stream().map(file -> {
      final String extension = ".".concat(FilenameUtils.getExtension(file.originalFileName()));
      final String fileName = file.s3().getFileName().concat(extension);

      return AppBoardFile.builder().board(appBoard).originalFileName(file.originalFileName())
          .fileName(fileName).fileUrl(file.s3().getUrl()).fileType("image")
          .fileSize(file.fileSize()).build();
    }).toList();

    writeBoardFileRepository.saveAll(insertFileList);
  }

  @CacheEvict(cacheNames = "getBoardDetail", key = "'board-id-' + #write.board.id")
  public boolean writeComment(AppBoardComment write) {
    AppBoardComment comment = writeCommentRepository.save(write);
    return comment.getId() > 0L;
  }

  @Caching(evict = {@CacheEvict(cacheNames = "getBoardDetail", key = "'board-id-' + #appBoard.id"),
      @CacheEvict(cacheNames = "getBoardList", allEntries = true),
      @CacheEvict(cacheNames = "getHomeBoardData", allEntries = true)})
  public int modifyBoard(AppBoard appBoard) {
    AppBoard originBoard = writeBoardRepository.findById(appBoard.getId())
        .orElseThrow(() -> new BusinessException("존재하지 않는 게시글입니다."));
    originBoard.update(appBoard.getTitle(), appBoard.getContent());
    return 1;
  }
}
