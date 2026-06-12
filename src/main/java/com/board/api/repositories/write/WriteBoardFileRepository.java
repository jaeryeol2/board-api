package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import com.board.api.entities.AppBoardFile;

public interface WriteBoardFileRepository extends JpaRepository<AppBoardFile, Long> {

}
