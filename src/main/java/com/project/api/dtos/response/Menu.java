package com.project.api.dtos.response;

import com.project.api.constants.MenuType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Menu {
  private long id;
  private Long parentId;
  private MenuType menuType;
  private String menuName;
  private String href;
  private boolean isActive;

}
