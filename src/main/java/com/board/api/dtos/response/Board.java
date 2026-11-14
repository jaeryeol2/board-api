package com.board.api.dtos.response;

import java.time.OffsetDateTime;
import java.util.List;
import com.board.api.constants.BoardType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    if (isAnonymous(boardTypeCode)) {
      return null;
    }

    return writerName;
  }

  /** 익명게시판은 작성자 로그인 ID를 내려주지 않습니다. */
  public String getWriterId() {
    return isAnonymous(boardTypeCode) ? null : writerId;
  }

  /** 링크 생성용 게시판 타입명 (예: BOARD_FREE). 응답 전용이며 역직렬화 시 무시합니다. */
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String getBoardType() {
    final BoardType type = BoardType.findByCode(boardTypeCode);
    return type != null ? type.name() : null;
  }

  private static boolean isAnonymous(final String boardTypeCode) {
    return BoardType.BOARD_ANONY.getCode().equals(boardTypeCode);
  }

  @Getter
  @ToString
  public static class BoardDetail {
    private long id;
    private String title;
    @Setter
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
    /** 요청자가 작성자인지 여부 (요청마다 서버에서 계산, JSON 필드명: author) */
    @Setter
    private boolean author;

    /** 익명게시판은 작성자 로그인 ID를 내려주지 않습니다. (작성자 여부는 author로 판단) */
    public String getWriterId() {
      return isAnonymous(boardTypeCode) ? null : writerId;
    }

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
