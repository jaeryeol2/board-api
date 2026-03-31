package com.project.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "password")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Join {
  private String userId;
  private String password;
  private String email;
  private String userName;
}
