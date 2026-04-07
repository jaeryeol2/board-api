package com.board.api.constants;

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
  BOARD_SPORTS("0002"),
  /** 연예게시판 */
  BOARD_ENTERTAIN("0003"),
  /** 음식게시판 */
  BOARD_FOOD("0004"),
  /** 유머게시판 */
  BOARD_HUMOR("0005"),
  /** 정보/팁게시판 */
  BOARD_INFO("0006"),
  /** 중고장터 */
  BOARD_MARKET("0007"),
  /** 질문/답변게시판 */
  BOARD_QNA("0008"),
  /** 건의사항 */
  BOARD_SUGGEST("0009"),
  /** 인기 */
  BOARD_POPULAR("0010"),
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
