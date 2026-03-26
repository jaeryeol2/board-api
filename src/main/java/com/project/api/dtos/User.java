package com.project.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = {"password"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
  private String userId;
  @JsonIgnore
  private String password;
  private String userName;
  private String userAddr;
  private String email;


  public User(final String userId, final String password) {
    this.userId = userId;
    this.password = password;
  }

  public User(final String userId, final String userName, final String userAddr,
      final String email) {
    this.userId = userId;
    this.userName = userName;
    this.userAddr = userAddr;
    this.email = email;
  }
}
