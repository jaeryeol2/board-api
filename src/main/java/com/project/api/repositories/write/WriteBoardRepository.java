package com.project.api.repositories.write;

import org.springframework.stereotype.Repository;
import com.project.api.dtos.Board.BoardFile;
import com.project.api.dtos.BoardWrite;
import com.project.api.entities.QAppBoard;
import com.project.api.entities.QAppBoardFile;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WriteBoardRepository {
  private final JPAQueryFactory factory;
  private static final QAppBoard entity = QAppBoard.appBoard;
  private static final QAppBoardFile fileEntity = QAppBoardFile.appBoardFile;

  public long writeBoard(BoardWrite write) {
    return factory.insert(entity)//
        .columns(entity.boardTypeCode, entity.title, entity.content, entity.groupId,
            entity.groupSeq, entity.groupDepth, entity.writerId)//
        .values(write.getBoardTypeCode(), write.getTitle(), write.getContent(), 0L, 0, 0,
            write.getWriterId())//
        .execute();
  }

  public long insertBoardFile(BoardFile file) {
    return factory.insert(fileEntity)//
        .columns(fileEntity)//
        .values(file.getId(), file.getOriginalFileName(), file.getFileUrl(), file.getFileType(),
            file.getFileSize())//
        .execute();
  }
}
