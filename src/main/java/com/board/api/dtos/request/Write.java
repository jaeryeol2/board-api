package com.board.api.dtos.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class NoticeWrite extends Write {
    private Long id;
  }

  @Setter
  @Getter
  @ToString(callSuper = true)
  @SuperBuilder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class CommentWrite {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 100, message = "댓글은 100자 이하로 입력해주세요.")
    private String content;
    private long boardId;
    private Long parentId;

  }
}
