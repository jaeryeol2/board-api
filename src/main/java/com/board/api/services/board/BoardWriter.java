package com.board.api.services.board;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.common.base.BusinessException;
import com.board.api.dtos.response.ResponseS3;
import com.board.api.entities.AppBoard;
import com.board.api.entities.AppBoardComment;
import com.board.api.entities.AppBoardFile;
import com.board.api.repositories.write.WriteBoardFileRepository;
import com.board.api.repositories.write.WriteBoardRepository;
import com.board.api.repositories.write.WriteCommentRepository;
import com.board.api.services.common.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class BoardWriter {
  private final S3Service s3Service;
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

  public void writeBoardFile(AppBoard appBoard, List<MultipartFile> fileList) {
    if (fileList == null || fileList.isEmpty()) {
      return;
    }

    List<AppBoardFile> insertFileList = new ArrayList<>();

    for (MultipartFile file : fileList) {
      if (file.isEmpty())
        continue;

      final ResponseS3 s3 = s3Service.uploadImage(file);
      if (s3 == null) {
        throw new BusinessException("파일업로드에 실패했습니다.");
      }

      final String originName = file.getOriginalFilename();
      final String extension = ".".concat(FilenameUtils.getExtension(originName));
      final String fileName = s3.getFileName().concat(extension);

      insertFileList.add(
          AppBoardFile.builder().board(appBoard).originalFileName(originName).fileName(fileName)
              .fileUrl(s3.getUrl()).fileType("image").fileSize(file.getSize()).build());
    }

    if (!insertFileList.isEmpty()) {
      writeBoardFileRepository.saveAll(insertFileList);
    }
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
