package com.project.api.dtos;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = "password")
public class Login {
  private String userId;
  private String password;
}
