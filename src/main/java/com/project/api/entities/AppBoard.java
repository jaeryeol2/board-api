package com.project.api.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_board")
@ToString
@Builder
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
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;


}
