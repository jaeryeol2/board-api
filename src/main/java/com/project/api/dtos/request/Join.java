package com.project.api.dtos.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString(exclude = "password")
public class Join {
  private String userId;
  private String password;
  private String email;
  private String userName;
}
