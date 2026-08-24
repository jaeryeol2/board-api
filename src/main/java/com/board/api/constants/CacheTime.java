package com.board.api.constants;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum CacheTime {
  /** access token */
  ACCESS_TOKEN("access_token", Duration.ofHours(1L)),
  /** refresh token */
  REFRESH_TOKEN("refresh_token", Duration.ofDays(30L)),
  /** traffic info */
  GET_TRAFFIC_INFO("getTrafficInfo", Duration.ofMinutes(1L)),
  /** user info */
  GET_USER_INFO("getUserInfo", Duration.ofMinutes(5L)),
  /** menu list */
  GET_MENU_LIST("getMenuList", Duration.ofHours(1L)),
  /** menu info */
  GET_MENU_INFO("getMenuInfo", Duration.ofHours(1L)),
  /** board list */
  BOARD_LIST("getBoardList", Duration.ofSeconds(30L)),
  /** board detail */
  BAORD_DETAIL("getBoardDetail", Duration.ofMinutes(5L)),
  /** home board list */
  HOME_BOARD_LIST("getHomeBoardData", Duration.ofHours(1L));

  private final String cacheName;
  private final Duration ttl;

  private CacheTime(String name, Duration time) {
    cacheName = name;
    ttl = time;
  }

  private static final Map<String, CacheTime> cacheMap = Map
      .copyOf(Stream.of(values()).collect(Collectors.toMap(CacheTime::name, Function.identity())));

  public static CacheTime find(String name) {
    if (name == null) {
      return null;
    }

    return cacheMap.get(name.toUpperCase());
  }
}
