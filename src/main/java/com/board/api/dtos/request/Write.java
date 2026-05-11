package com.board.api.dtos.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Write {
  private String title;
  private String content;
  @JsonIgnore
  private String writerId;

  @Setter
  @Getter
  @ToString(callSuper = true)
  @SuperBuilder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class BoardWrite extends Write {
    private Long id;
    private String boardTypeCode;
    private List<MultipartFile> attachments;
  }

  @Setter
  @Getter
  @ToString(callSuper = true)
  @SuperBuilder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class NoticeWrite extends Write {
    private Long id;
  }

  @Setter
  @Getter
  @ToString(callSuper = true)
  @SuperBuilder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class CommentWrite {
    private String content;
    private long boardId;
    private Long parentId;

  }
}
