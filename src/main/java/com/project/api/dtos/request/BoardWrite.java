package com.project.api.dtos.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
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
