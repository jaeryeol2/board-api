package com.project.api.dtos;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User {
  private String userId;
  private String userName;
  private String userAddr;
}
