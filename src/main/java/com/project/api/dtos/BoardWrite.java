package com.project.api.dtos;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class BoardWrite {
  private Long id;
  private String title;
  private String content;
  private String boardTypeCode;
  @JsonIgnore
  private String writerId;

  private List<MultipartFile> attachments;
}
