package com.project.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.project.api.dtos.Board;
import com.project.api.dtos.BoardFilter;
import com.project.api.entities.QAppBoard;
import com.project.api.entities.QAppBoardComment;
import com.project.api.entities.QAppBoardFile;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadBoardRepository {
  private final JPAQueryFactory factory;

  private static final QAppBoard board = QAppBoard.appBoard;
  private static final QAppBoardFile file = QAppBoardFile.appBoardFile;
  private static final QAppBoardComment comment = QAppBoardComment.appBoardComment;


  public List<Board> findBoardListByFilter(BoardFilter filter) {
    return factory
        .select(QAppBoard.create(board.id, board.title, board.writerId, board.writerName,
            board.createdAt))
        .from(board)//
        .where(board.boardTypeCode.eq(filter.getBoardTypeCode()))//
        .fetch()//
        .stream()//
        .map(item -> new Board(item.getId(), item.getTitle(), item.getWriterId(),
            item.getWriterName(), item.getCreatedAt()))//
        .toList();
  }

}
