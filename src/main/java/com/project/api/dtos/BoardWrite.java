package com.project.api.dtos;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardWrite {
  private String title;
  private String content;
  @Setter
  private String boardTypeCode;
  private List<MultipartFile> fileList;
}
