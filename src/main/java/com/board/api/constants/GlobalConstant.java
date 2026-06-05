package com.board.api.constants;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum GlobalConstant {
  ACCESS_TOKEN("access_token"), //
  REFRESH_TOKEN("auth_token"), //
  X_FORWARDED_FOR("X-Forwarded-For"), //
  X_GUEST_ID("X-Guest-Id");

  private String value;

  private GlobalConstant(String value) {
    this.value = value;
  }

  private static final Map<String, GlobalConstant> contstanteMap = Map.copyOf(
      Stream.of(values()).collect(Collectors.toMap(GlobalConstant::getValue, Function.identity())));

  public static GlobalConstant find(String value) {
    if (value == null) {
      return null;
    }

    return contstanteMap.get(value.toUpperCase());
  }
}
