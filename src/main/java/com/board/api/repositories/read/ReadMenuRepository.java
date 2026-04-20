package com.board.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.board.api.common.utils.QueryDslUtil;
import com.board.api.dtos.response.Menu;
import com.board.api.entities.QAppMenu;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadMenuRepository {
  private final JPAQueryFactory factory;
  private static final QAppMenu entity = QAppMenu.appMenu;

  public List<Menu> findMenuList() {
    final QBean<Menu> columns = Projections.fields(Menu.class, entity.id, entity.parentId,
        entity.menuType, entity.boardCode, entity.menuName, entity.href, entity.isActive);
    return factory.select(columns)//
        .from(entity)//
        .where(entity.isActive.eq(true))//
        .orderBy(entity.sortOrder.asc())//
        .fetch();
  }

  public Menu findMenuByCode(String boardTypeCode) {
    final QBean<Menu> columns = QueryDslUtil.generateFields(Menu.class, entity);

    return factory.select(columns)//
        .from(entity)//
        .where(entity.boardCode.eq(boardTypeCode)//
            .and(entity.parentId.isNull()))//
        .fetchOne();
  }
}
