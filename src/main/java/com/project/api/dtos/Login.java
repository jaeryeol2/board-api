package com.project.api.dtos;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Login {
  @Getter
  private String loginId;
  private String password;
}
