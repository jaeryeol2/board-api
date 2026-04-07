package com.project.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.api.entities.AppBoard;

@Repository
public interface WriteBoardRepository extends JpaRepository<AppBoard, Long> {

  @Modifying
  @Query("UPDATE AppBoard b SET b.viewCount = b.viewCount + 1 WHERE b.id = :boardId")
  public int updateViewCount(@Param("boardId") long boardId);
}
