package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import com.board.api.entities.AppBoardComment;

public interface WriteCommentRepository extends JpaRepository<AppBoardComment, Long> {

}
