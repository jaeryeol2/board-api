package com.board.api.dtos.response;

import java.time.OffsetDateTime;
import java.util.List;
import com.board.api.constants.BoardType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Board {
  private long id;
  private String title;
  private String boardTypeCode;
  private String writerId;
  private String writerName;
  private OffsetDateTime createdAt;
  private int viewCount;

  public String getWriterName() {
    if (BoardType.BOARD_ANONY.getCode().equals(boardTypeCode)) {
      return null;
    }

    return writerName;
  }

  @Getter
  @ToString
  public static class BoardDetail {
    private long id;
    private String title;
    private String content;
    private String boardTypeCode;
    private String writerId;
    private String writerName;
    private int viewCount;
    private long likeCount;
    private boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
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
    private OffsetDateTime createdAt;
  }

}
