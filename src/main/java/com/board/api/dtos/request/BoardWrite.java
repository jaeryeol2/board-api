package com.board.api.dtos.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class BoardWrite {
  private Long id;
  private String title;
  private String content;
  @Setter
  private String boardTypeCode;
  @Setter
  @JsonIgnore
  private String writerId;

  private List<MultipartFile> attachments;
}
