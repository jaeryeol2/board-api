package com.project.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.api.entities.AppBoard;

@Repository
public interface WriteBoardRepository extends JpaRepository<AppBoard, Long> {
}
