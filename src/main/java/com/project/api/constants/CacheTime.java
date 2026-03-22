package com.project.api.constants;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum CacheTime {
  /** access token */
  ACCESS_TOKEN("access_token", Duration.ofHours(1)),
  /** refresh token */
  REFRESH_TOKEN("refresh_token", Duration.ofDays(30));

  private final String cacheName;
  private final Duration ttl;

  CacheTime(String name, Duration time) {
    cacheName = name;
    ttl = time;
  }

  private static final Map<String, CacheTime> cacheMap = Collections.unmodifiableMap(
      Stream.of(values()).collect(Collectors.toMap(CacheTime::getCacheName, Function.identity())));

  public static CacheTime find(String cacheName) {
    return cacheMap.get(cacheName);
  }
}
