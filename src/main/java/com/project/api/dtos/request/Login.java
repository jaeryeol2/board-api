package com.project.api.dtos.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString(exclude = "password")
public class Login {
  private String userId;
  private String password;
}
