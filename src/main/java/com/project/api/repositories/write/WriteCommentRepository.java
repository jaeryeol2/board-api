package com.project.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.api.entities.AppBoardComment;

@Repository
public interface WriteCommentRepository extends JpaRepository<AppBoardComment, Long> {

}
