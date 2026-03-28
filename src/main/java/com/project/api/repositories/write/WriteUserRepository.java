package com.project.api.repositories.write;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import com.project.api.dtos.Join;
import com.project.api.entities.QAppUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WriteUserRepository {
  private final PasswordEncoder passwordEncoder;
  private final JPAQueryFactory factory;
  private static final QAppUser entity = QAppUser.appUser;

  public long updateLoginTime(final String userId) {
    return factory.update(entity)//
        .set(entity.lastLoginAt, LocalDateTime.now())//
        .where(entity.userId.eq(userId))//
        .execute();
  }

  public long insertJoin(Join join) {
    return factory.insert(entity)//
        .columns(entity.userId, entity.password, entity.userName, entity.email, entity.userRole)//
        .values(join.getUserId(), passwordEncoder.encode(join.getPassword()), join.getUserName(),
            join.getEmail(), "editor")//
        .execute();
  }
}
