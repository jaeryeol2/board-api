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
}

