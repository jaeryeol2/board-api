package com.project.api.dtos.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString(exclude = "password")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Login {
  private String userId;
  private String password;
}
