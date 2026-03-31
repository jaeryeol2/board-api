package com.project.api.entities;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_board_file")
@ToString(exclude = "board")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
  private LocalDateTime createdAt;

  public AppBoardFile(long boardId, String originalFileName, String fileName, String fileUrl,
      String fileType, long fileSize) {
    this.board = new AppBoard(boardId, null, null, null);
    this.originalFileName = originalFileName;
    this.fileName = fileName;
    this.fileUrl = fileUrl;
    this.fileType = fileType;
    this.fileSize = fileSize;

  }
}
