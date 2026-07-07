package com.board.api.entities;

import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_board")
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppBoard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String boardTypeCode;
  private String title;
  private String content;
  private String writerId;
  @Builder.Default
  private Integer viewCount = 0;
  @Builder.Default
  private boolean isDeleted = false;

  // --- 계층형 답글 관련 필드 추가 ---
  private Long groupId;
  @Builder.Default
  private Integer groupSeq = 0;
  @Builder.Default
  private Integer groupDepth = 0;
  // ----------------------------------

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;
  @UpdateTimestamp
  private OffsetDateTime updatedAt;

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

}
