package com.project.api.repositories.read;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import com.project.api.dtos.User;
import com.project.api.entities.AppUser;
import com.project.api.entities.QAppUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadUserRepository {
  private final JPAQueryFactory factory;
  private static final QAppUser entity = QAppUser.appUser;

  public User findLoginInfoById(final String userId) {
    final AppUser user = factory.select(QAppUser.create(entity.userId, entity.password))
        .from(entity).where(entity.userId.eq(userId)).fetchOne();

    return ObjectUtils.isEmpty(user) ? null : new User(user.getUserId(), user.getPassword());
  }

  public User findUserById(final String userId) {
    final AppUser user = factory
        .select(Projections.fields(AppUser.class, entity.userId, entity.userName, entity.email))
        .from(entity).where(entity.userId.eq(userId)).fetchOne();

    return ObjectUtils.isEmpty(user) ? null
        : new User(user.getUserId(), user.getUserName(), user.getUserAddr(), user.getEmail());
  }
}
