package com.project.api.entities;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_board")
@ToString
public class AppBoard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String boardTypeCode;
  private String title;
  private String content;
  private String writerId;
  private String writerName;
  private Integer viewCount = 0;
  private boolean isNotice = false;
  private boolean isDeleted = false;

  // --- 계층형 답글 관련 필드 추가 ---
  private Long groupId;
  private Integer groupSeq = 0;
  private Integer groupDepth = 0;
  // ----------------------------------

  @Column(updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @QueryProjection
  public AppBoard(long id, String title, String writerId, String writerName,
      LocalDateTime createdAt) {
    this.id = id;
    this.title = title;
    this.writerId = writerId;
    this.writerName = writerName;
    this.createdAt = createdAt;
  }
}
