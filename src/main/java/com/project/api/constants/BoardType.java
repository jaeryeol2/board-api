package com.project.api.constants;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum BoardType {
  BOARD_GAME("0001");

  private String code;

  private BoardType(String code) {
    this.code = code;
  }

  private static final Map<String, BoardType> boardTypeMap = Collections.unmodifiableMap(
      Stream.of(values()).collect(Collectors.toMap(BoardType::name, Function.identity())));

  public static BoardType find(String name) {
    if (name == null) {
      return null;
    }

    return boardTypeMap.get(name.toUpperCase());
  }
}
