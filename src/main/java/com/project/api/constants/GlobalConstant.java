package com.project.api.constants;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum GlobalConstant {
  ACCESS_TOKEN("access_token"), REFRESH_TOKEN("auth_token");

  private String value;

  private GlobalConstant(String value) {
    this.value = value;
  }

  private static final Map<String, GlobalConstant> contstanteMap = Collections.unmodifiableMap(
      Stream.of(values()).collect(Collectors.toMap(GlobalConstant::getValue, Function.identity())));

  public static GlobalConstant find(String value) {
    return contstanteMap.get(value);
  }
}
