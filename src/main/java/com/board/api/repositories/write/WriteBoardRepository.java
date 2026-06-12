package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.board.api.entities.AppBoard;

public interface WriteBoardRepository extends JpaRepository<AppBoard, Long> {

  @Modifying
  @Query("UPDATE AppBoard b SET b.viewCount = b.viewCount + 1 WHERE b.id = :boardId")
  public int updateViewCount(@Param("boardId") long boardId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE AppBoard b SET b.title = :title, b.content = :content WHERE b.id = :boardId")
  public int updateBoard(@Param("boardId") long boardId, @Param("title") String title,
      @Param("content") String content);
}
