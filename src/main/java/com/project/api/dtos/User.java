package com.project.api.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = {"password"})
@NoArgsConstructor
public class User {
  private String userId;
  private String password;
  private String userName;
  private String userAddr;


  public User(final String userId, final String password) {
    this.userId = userId;
    this.password = password;
  }
}
