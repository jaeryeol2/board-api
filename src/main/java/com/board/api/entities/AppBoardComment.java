package com.board.api.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_board_comment")
@ToString(exclude = "board")
@Builder
public class AppBoardComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private AppBoard board;

  private Long parentId;
  private String writerId;
  private String content;
  @Builder.Default
  private boolean isDeleted = false;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
