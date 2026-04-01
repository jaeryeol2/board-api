package com.project.api.dtos.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
