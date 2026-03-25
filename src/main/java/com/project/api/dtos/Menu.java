package com.project.api.dtos;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Menu {
  private String menuName;
  private String href;
  private boolean use;

  public Menu(String menuName, String href, boolean use) {
    this.menuName = menuName;
    this.href = href;
    this.use = use;
  }
}
