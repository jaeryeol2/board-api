package com.board.api.dtos.response;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Notice {
  private long id;
  private String title;
  private String content;
  private String writerName;
  private OffsetDateTime createdAt;
}
