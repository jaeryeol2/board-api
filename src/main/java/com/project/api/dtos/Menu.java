package com.project.api.dtos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
