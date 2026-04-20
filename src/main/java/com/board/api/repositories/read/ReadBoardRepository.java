package com.board.api.repositories.read;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.board.api.common.utils.QueryDslUtil;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.dtos.response.Board.BoardFile;
import com.board.api.entities.QAppBoard;
import com.board.api.entities.QAppBoardComment;
import com.board.api.entities.QAppBoardFile;
import com.board.api.entities.QAppBoardLike;
import com.board.api.entities.QAppUser;
import com.querydsl.core.types.Projections;
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

  public List<Long> findBoardByIds(BoardFilter filter) {
    return factory.select(board.id)//
        .from(board)//
        .where(board.boardTypeCode.eq(filter.getBoardTypeCode()))//
        .limit(filter.getRowsPerPage())//
        .offset((long) (filter.getPage() - 1) * filter.getRowsPerPage())
        .orderBy(board.createdAt.desc(), board.id.desc()).fetch();
  }

  public List<Board> findBoardList(String boardTypeCode, List<Long> ids) {
    final QBean<Board> colums = QueryDslUtil.generateFields(Board.class, board,
        board.boardTypeCode.when(BoardType.BOARD_ANONY.getCode())//
            .then("")//
            .otherwise(user.userName).as(WRITER_NAME));

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
        board.boardTypeCode//
            .when(BoardType.BOARD_ANONY.getCode())//
            .then("")//
            .otherwise(user.userName).as(WRITER_NAME),
        like.count().as("likeCount"));

    return factory.select(colums)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .leftJoin(like).on(board.id.eq(like.id))//
        .where(board.id.eq(id))//
        .groupBy(board.id, user.userName)//
        .fetchOne();
  }

  public List<BoardComment> findBoardCommentByBoardId(final long boardId) {
    final QBean<BoardComment> columns = QueryDslUtil.generateFields(BoardComment.class, comment,
        comment.board.boardTypeCode//
            .when(BoardType.BOARD_ANONY.getCode())//
            .then("익명")//
            .otherwise(user.userName).as(WRITER_NAME));
    return factory.select(columns)//
        .from(comment)//
        .join(board)//
        .on(comment.board.id.eq(board.id))//
        .leftJoin(user)//
        .on(comment.writerId.eq(user.userId))//
        .where(comment.board.id.eq(boardId)//
            .and(comment.isDeleted.eq(false)))//
        .orderBy(comment.createdAt.asc())//
        .fetch();
  }

  public List<BoardFile> findBoardFileByBoardId(final long boardId) {
    final QBean<BoardFile> fileColumns = QueryDslUtil.generateFields(BoardFile.class, file);

    return factory.select(fileColumns)//
        .from(file)//
        .where(file.board.id.eq(boardId))//
        .fetch();
  }

  public long findBoardPopularCount() {
    return factory.select(board.id.count())//
        .from(board)//
        .where(board.viewCount.goe(30))//
        .fetchOne();
  }

  public List<Long> findBoardPopularByIds(BoardFilter filter) {
    return factory.select(board.id)//
        .from(board)//
        .where(board.viewCount.goe(30))//
        .limit(filter.getRowsPerPage())//
        .offset((long) (filter.getPage() - 1) * filter.getRowsPerPage())
        .orderBy(board.createdAt.desc(), board.id.desc()).fetch();
  }

  public List<Board> findBoardPopularList(List<Long> ids) {
    final QBean<Board> columns =
        QueryDslUtil.generateFields(Board.class, board, user.userName.as(WRITER_NAME));

    return factory.select(columns)//
        .from(board)//
        .leftJoin(user)//
        .on(board.writerId.eq(user.userId))//
        .where(board.id.in(ids))//
        .orderBy(board.createdAt.desc(), board.id.desc())//
        .fetch();
  }

  public List<Board> findPopularBoardListByBoardTypeCode(String boardTypeCode) {
    final QBean<Board> columns = Projections.fields(Board.class, board.id, board.boardTypeCode,
        board.title, board.viewCount);
    final LocalDateTime from = LocalDateTime.now().minusDays(1L);
    final LocalDateTime now = LocalDateTime.now();

    return factory.select(columns)//
        .from(board)//
        .where(board.boardTypeCode.eq(boardTypeCode)//
            .and(board.isDeleted.eq(false))//
            .and(board.createdAt.between(from, now)//
                .or(board.updatedAt.between(from, now)))//
            .and(board.viewCount.goe(0L)))//
        .orderBy(board.viewCount.desc())//
        .limit(10L)//
        .fetch();
  }

}
