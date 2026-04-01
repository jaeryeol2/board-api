package com.project.api.dtos.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Board {
  private long id;
  private String title;
  private String writerId;
  private String writerName;
  private LocalDateTime createdAt;
  private int viewCount;

  @Getter
  @ToString
  public static class BoardDetail {
    private long id;
    private String title;
    private String content;
    private String writerId;
    private String writerName;
    private int viewCount;
    private long likeCount;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Setter
    private List<BoardComment> commentList;
    @Setter
    private List<BoardFile> fileList;

  }

  @Getter
  @ToString
  public static class BoardComment {
    private long id;
    private long parentId;
    private String content;
    private String writerId;
    private String writerName;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
  }

  @Getter
  @ToString
  public static class BoardFile {
    private long id;
    private long boardId;
    private String originalFileName;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private LocalDateTime createdAt;
  }

}
