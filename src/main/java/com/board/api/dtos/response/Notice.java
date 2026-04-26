package com.board.api.dtos.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Notice {
  private long id;
  private String title;
  private String content;
  private String writerName;
  private LocalDateTime createdAt;
}
