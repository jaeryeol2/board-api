package com.project.api.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Board {
  private long id;
  private String title;
  private String writerId;
  private String writerName;
  private LocalDateTime createdAt;
}
