package com.project.api.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = {"password"})
public class User {
  private String userId;
  @JsonIgnore
  private String password;
  private String userName;
  private String userAddr;
  private String email;

}
