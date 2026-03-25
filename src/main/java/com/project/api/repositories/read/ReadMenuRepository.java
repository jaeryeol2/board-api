package com.project.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.project.api.dtos.Menu;
import com.project.api.entities.AppMenu;
import com.project.api.entities.QAppMenu;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadMenuRepository {
  private final JPAQueryFactory factory;
  private static final QAppMenu entity = QAppMenu.appMenu;

  public List<Menu> selectMenuList() {
    List<AppMenu> list = factory.selectFrom(entity).where(entity.isActive.eq(true)).fetch();

    return list.stream().map(item -> new Menu(item.getMenuName(), item.getHref(), item.isActive()))
        .toList();
  }
}
