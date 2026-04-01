package com.project.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.project.api.common.utils.QueryDslUtil;
import com.project.api.dtos.request.BoardFilter;
import com.project.api.dtos.response.Board;
import com.project.api.dtos.response.Board.BoardComment;
import com.project.api.dtos.response.Board.BoardDetail;
import com.project.api.dtos.response.Board.BoardFile;
import com.project.api.entities.QAppBoard;
import com.project.api.entities.QAppBoardComment;
import com.project.api.entities.QAppBoardFile;
import com.project.api.entities.QAppBoardLike;
import com.project.api.entities.QAppUser;
import com.querydsl.core.types.QBean;
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
  private static final String WRITER_NAME = "writerName";

  public long findBoardCountById(BoardFilter filter) {
    return factory.select(board.id.count())//
        .from(board)//
        .where(board.boardTypeCode.eq(filter.getBoardTypeCode()))//
        .fetchOne();
  }

  public List<Long> findBoardById(BoardFilter filter) {
    return factory.select(board.id)//
        .from(board)//
        .where(board.boardTypeCode.eq(filter.getBoardTypeCode()))//
        .limit(filter.getRowsPerPage())//
        .offset((long) (filter.getPage() - 1) * filter.getRowsPerPage())
        .orderBy(board.createdAt.desc(), board.id.desc()).fetch();
  }

  public List<Board> findBoardList(String boardTypeCode, List<Long> ids) {
    final QBean<Board> colums =
        QueryDslUtil.generateFields(Board.class, board, user.userName.as(WRITER_NAME));

    return factory.select(colums)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .where(board.boardTypeCode.eq(boardTypeCode)//
            .and(board.id.in(ids)))//
        .orderBy(board.createdAt.desc(), board.id.desc())//
        .fetch();
  }

  public BoardDetail findBoardById(final long id) {
    final QBean<BoardDetail> colums = QueryDslUtil.generateFields(BoardDetail.class, board,
        user.userName.as(WRITER_NAME), like.count().as("likeCount"));
    final QBean<BoardComment> commentColumns =
        QueryDslUtil.generateFields(BoardComment.class, comment, user.userName.as(WRITER_NAME));
    final QBean<BoardFile> fileColumns = QueryDslUtil.generateFields(BoardFile.class, file);
    final BoardDetail detail = factory.select(colums)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .leftJoin(like).on(board.id.eq(like.id))//
        .where(board.id.eq(id))//
        .groupBy(board.id, user.userName)//
        .fetchOne();

    final List<BoardComment> commentList = factory.select(commentColumns)//
        .from(comment)//
        .leftJoin(user)//
        .on(comment.writerId.eq(user.userId))//
        .where(comment.id.eq(id)//
            .and(comment.isDeleted.eq(false)))//
        .fetch();
    final List<BoardFile> fileList = factory.select(fileColumns)//
        .from(file)//
        .where(file.board.id.eq(id)).fetch();


    detail.setCommentList(commentList);
    detail.setFileList(fileList);

    return detail;

  }

}
