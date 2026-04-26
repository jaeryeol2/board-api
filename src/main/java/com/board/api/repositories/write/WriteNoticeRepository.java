package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import com.board.api.entities.AppNotice;

public interface WriteNoticeRepository extends JpaRepository<AppNotice, Long> {
}
