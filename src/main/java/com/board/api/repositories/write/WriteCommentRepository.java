package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.board.api.entities.AppBoardComment;

@Repository
public interface WriteCommentRepository extends JpaRepository<AppBoardComment, Long> {

}
