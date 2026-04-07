package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.board.api.entities.AppBoardFile;

@Repository
public interface WriteBoardFileRepository extends JpaRepository<AppBoardFile, Long> {

}
