package com.board.api.constants;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum ProfileActive {
  LOCAL("local"), DEV("dev"), PROD("prod");

  private String value;

  ProfileActive(String value) {
    this.value = value;
  }

  private static final Map<String, ProfileActive> activeMap = Map.copyOf(
      Stream.of(values()).collect(Collectors.toMap(ProfileActive::name, Function.identity())));

  public static ProfileActive find(String name) {
    if (name == null) {
      return null;
    }

    return activeMap.get(name);
  }
}
