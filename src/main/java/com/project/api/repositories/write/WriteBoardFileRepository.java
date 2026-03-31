package com.project.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.api.entities.AppBoardFile;

@Repository
public interface WriteBoardFileRepository extends JpaRepository<AppBoardFile, Long> {

}
