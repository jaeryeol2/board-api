package com.board.api.dtos.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardFilter {
  @Setter
  private String boardTypeCode;
  private String title;
  private Long id;
  private String writerName;
  private int page;
  @Builder.Default
  private int rowsPerPage = 20;

}
