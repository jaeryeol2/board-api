package com.board.api.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.board.api.entities.AppUser;

@Repository
public interface WriteUserRepository extends JpaRepository<AppUser, Long> {

  @Modifying
  @Query("UPDATE AppUser u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
  public int updateLastLoginDate(@Param("userId") String userId);
}
