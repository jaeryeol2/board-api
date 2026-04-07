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
@Table(name = "app_board_file")
@ToString(exclude = "board")
@Builder
public class AppBoardFile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private AppBoard board;

  private String originalFileName;
  private String fileName;
  private String fileUrl;
  private String fileType;
  private Long fileSize;

  @Column(updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

}
