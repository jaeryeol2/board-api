package com.board.api.dtos.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PersonalUser extends User {
  private String userAddr;
  @Setter
  private List<Board> writedContentList;
  @Setter
  private List<Board.BoardComment> writedCommentList;
  /** 작성 글 전체 개수 (writedContentList는 최근 일부만 포함) */
  @Setter
  private long writedContentCount;
  /** 작성 댓글 전체 개수 (writedCommentList는 최근 일부만 포함) */
  @Setter
  private long writedCommentCount;
}

