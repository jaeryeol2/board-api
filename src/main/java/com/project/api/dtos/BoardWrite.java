package com.project.api.dtos;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class BoardWrite {
  private String title;
  private String content;
  @Setter
  private String boardTypeCode;
  private List<MultipartFile> fileList;
}
