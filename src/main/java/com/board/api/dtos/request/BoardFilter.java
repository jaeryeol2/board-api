package com.board.api.dtos.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@ToString
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

  private static final int MAX_ROWS_PER_PAGE = 50;

  /** 1 미만(미전달 시 0)이면 1페이지로 보정합니다. (음수 offset 방지) */
  public int getPage() {
    return Math.max(page, 1);
  }

  /** 1~MAX_ROWS_PER_PAGE 범위로 보정합니다. (대량 조회·캐시 오염 방지) */
  public int getRowsPerPage() {
    return Math.clamp(rowsPerPage, 1, MAX_ROWS_PER_PAGE);
  }

}
