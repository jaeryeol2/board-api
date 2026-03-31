package com.project.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.project.api.dtos.Board;
import com.project.api.dtos.Board.BoardComment;
import com.project.api.dtos.Board.BoardDetail;
import com.project.api.dtos.Board.BoardFile;
import com.project.api.dtos.BoardFilter;
import com.project.api.entities.AppBoardComment;
import com.project.api.entities.AppBoardFile;
import com.project.api.entities.QAppBoard;
import com.project.api.entities.QAppBoardComment;
import com.project.api.entities.QAppBoardFile;
import com.project.api.entities.QAppBoardLike;
import com.project.api.entities.QAppUser;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadBoardRepository {
  private final JPAQueryFactory factory;

  private static final QAppUser user = QAppUser.appUser;
  private static final QAppBoard board = QAppBoard.appBoard;
  private static final QAppBoardFile file = QAppBoardFile.appBoardFile;
  private static final QAppBoardComment comment = QAppBoardComment.appBoardComment;
  private static final QAppBoardLike like = QAppBoardLike.appBoardLike;

  public List<Board> findBoardListByFilter(BoardFilter filter) {
    final ConstructorExpression<Board> colums = Projections.constructor(Board.class, board.id,
        board.title, board.writerId, user.userName.as("writerName"), board.createdAt);

    return factory.select(colums)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .where(board.boardTypeCode.eq(filter.getBoardTypeCode()))//
        .orderBy(board.createdAt.desc())//
        .fetch();
  }

  public BoardDetail findBoardById(final long id) {
    final ConstructorExpression<BoardDetail> colums =
        Projections.constructor(BoardDetail.class, board.id, board.title, board.content,
            board.writerId, user.userName.as("writerName"), board.viewCount,
            like.count().as("likeCount"), board.isDeleted, board.createdAt, board.updatedAt);
    final BoardDetail detail = factory.select(colums)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .leftJoin(like).on(board.id.eq(like.id)).where(board.id.eq(id))//
        .groupBy(board.id, user.userName).fetchOne();
    final List<AppBoardComment> commentList = factory.selectFrom(comment)//
        .where(comment.id.eq(id)//
            .and(comment.isDeleted.eq(false)))//
        .fetch();
    final List<AppBoardFile> fileList = factory.selectFrom(file)//
        .where(file.id.eq(id)).fetch();


    detail.setCommentList(commentList
        .stream().map(c -> new BoardComment(c.getId(), c.getParentId(), c.getContent(),
            c.getWriterId(), c.getWriterName(), c.isDeleted(), c.getCreatedAt(), c.getUpdatedAt()))
        .toList());
    detail.setFileList(fileList.stream()
        .map(f -> new BoardFile(f.getId(), f.getBoard().getId(), f.getOriginalFileName(),
            f.getFileName(), f.getFileUrl(), f.getFileType(), f.getFileSize(), f.getCreatedAt()))
        .toList());

    return detail;

  }

}
