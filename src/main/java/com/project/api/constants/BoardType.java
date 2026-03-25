package com.project.api.constants;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum BoardType {
  /** 자유게시판 */
  BOARD_FREE("0001"),
  /** 스포츠게시판 */
  BOARD_SPORT("0002"),
  /** 연예게시판 */
  BOARD_ENTERTAIN("0003"),
  /** 음식게시판 */
  BOARD_FOOD("0004"),
  /** 익명게시판 */
  BOARD_ANONY("9999");

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
