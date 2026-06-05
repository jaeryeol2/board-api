package com.board.api.constants;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum UserRole {
  USER_GENERAL("0001"), //
  USER_ADMIN("9999");

  private String code;

  private UserRole(String code) {
    this.code = code;
  }

  private static final Map<String, UserRole> userRoleMap = Map
      .copyOf(Stream.of(values()).collect(Collectors.toMap(UserRole::name, Function.identity())));

  public static UserRole find(String name) {
    if (name == null) {
      return null;
    }

    return userRoleMap.get(name.toUpperCase());
  }
}
