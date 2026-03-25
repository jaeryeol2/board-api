package com.project.api.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class BoardFilter {
  @Setter
  private String boardTypeCode;
  private String title;
  private Long id;
  private String writerName;

}
