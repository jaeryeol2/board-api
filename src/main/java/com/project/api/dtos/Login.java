package com.project.api.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class Login {
  @Getter
  private String loginId;
  private String password;
}
