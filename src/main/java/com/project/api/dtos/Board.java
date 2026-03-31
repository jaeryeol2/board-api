package com.project.api.dtos;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class Board {
  private final long id;
  private final String title;
  private final String writerId;
  private final String writerName;
  private final LocalDateTime createdAt;

  @Getter
  @ToString
  @RequiredArgsConstructor
  public static class BoardDetail {
    private final long id;
    private final String title;
    private final String content;
    private final String writerId;
    private final String writerName;
    private final int viewCount;
    private final long likeCount;
    private final boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    @Setter
    private List<BoardComment> commentList;
    @Setter
    private List<BoardFile> fileList;

  }

  @Getter
  @ToString
  @RequiredArgsConstructor
  public static class BoardComment {
    private final long id;
    private final long parentId;
    private final String content;
    private final String writerId;
    private final String writerName;
    private final boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
  }

  @Getter
  @ToString
  @RequiredArgsConstructor
  public static class BoardFile {
    private final long id;
    private final long boardId;
    private final String originalFileName;
    private final String fileName;
    private final String fileUrl;
    private final String fileType;
    private final long fileSize;
    private final LocalDateTime createdAt;
  }


}
