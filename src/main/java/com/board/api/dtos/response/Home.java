package com.board.api.dtos.response;

import java.util.List;
import com.board.api.constants.BoardType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Home {
  private String menuName;
  private BoardType basePath;
  private String href;
  private List<Board> boards;
}
