package com.board.api.repositories.write;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.board.api.entities.AppUser;

public interface WriteUserRepository extends JpaRepository<AppUser, Long> {

  public Optional<AppUser> findByUserId(String userId);

}
