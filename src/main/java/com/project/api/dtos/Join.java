package com.project.api.dtos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "password")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Join {
  private String userId;
  private String password;
  private String email;
  private String userName;
}
