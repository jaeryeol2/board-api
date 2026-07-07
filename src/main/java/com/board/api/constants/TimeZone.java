package com.board.api.constants;

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum TimeZone {
  SEOUL("Asia/Seoul");

  private String value;

  TimeZone(String value) {
    this.value = value;
  }

  private static final Map<String, TimeZone> timeZoneMap = Map
      .copyOf(Stream.of(values()).collect(Collectors.toMap(TimeZone::name, Function.identity())));

  public static TimeZone find(String name) {
    if (name == null) {
      return null;
    }

    return timeZoneMap.get(name);
  }

  public ZoneId getZoneId() {
    return ZoneId.of(this.value);
  }
}
