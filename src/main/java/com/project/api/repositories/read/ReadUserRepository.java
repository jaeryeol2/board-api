package com.project.api.repositories.read;

import org.springframework.stereotype.Repository;
import com.project.api.dtos.response.User;
import com.project.api.entities.QAppUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadUserRepository {
  private final JPAQueryFactory factory;
  private static final QAppUser entity = QAppUser.appUser;

  public User findLoginInfoById(final String userId) {
    final QBean<User> columns = Projections.fields(User.class, entity.userId, entity.password);
    return factory.select(columns)//
        .from(entity)//
        .where(entity.userId.eq(userId)//
            .and(entity.isActive.eq(true)))//
        .fetchOne();
  }

  public User findUserById(final String userId) {
    final QBean<User> columns = Projections.fields(User.class, entity.userId, entity.userName,
        entity.userRole, entity.userAddr);
    return factory.select(columns)//
        .from(entity)//
        .where(entity.userId.eq(userId)//
            .and(entity.isActive.eq(true)))//
        .fetchOne();
  }
}
