package com.project.api.dtos.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Menu {
  private String menuName;
  private String href;
  private boolean isActive;

}
