package com.board.api.services.user;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadUserRepository;
import com.board.api.services.common.CommonReader;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReader {
  private final TokenUtil tokenUtil;
  private final ReadUserRepository readUserRepository;
  private final CommonReader commonReader;

  @Cacheable(cacheNames = "getUserInfo", key = "#userId", unless = "#result == null")
  public User getUserInfo(String userId) {
    return readUserRepository.findUserById(userId);
  }

  public User getLoginUserInfo(String userId) {
    return readUserRepository.findLoginInfoById(userId);
  }

  public String getUserId(HttpServletRequest request) {
    String token = getAccessToken(request);

    if (token == null) {
      token = getRefreshToken(request);
    }

    return token != null ? tokenUtil.getTokenInfo(token).getSubject() : null;
  }

  public boolean checkToken(HttpServletRequest request) {
    String token = getAccessToken(request);

    if (token == null) {
      token = getRefreshToken(request);
    }

    return !tokenUtil.isTokenExpired(token);
  }

  public String getAccessToken(HttpServletRequest request) {
    String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (accessToken != null) {
      final String token = accessToken.replaceAll("^(?i)Bearer\\s+", "");
      return !tokenUtil.isTokenExpired(token) ? token : null;
    }

    return null;
  }

  public String getRefreshToken(HttpServletRequest request) {
    final Cookie[] cookies =
        Objects.requireNonNullElseGet(request.getCookies(), () -> new Cookie[0]);
    final String token = Arrays.asList(cookies).stream()//
        .filter(Objects::nonNull)//
        .filter(cookie -> REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName()))//
        .findFirst()//
        .orElseGet(() -> {
          final Cookie cookie = new Cookie(REFRESH_TOKEN.getValue(), null);
          cookie.setHttpOnly(true);
          cookie.setSecure(true);

          return cookie;
        })//
        .getValue();

    return !tokenUtil.isTokenExpired(token) ? token : null;
  }

  public ResponseApi<Boolean> setGuest(final String newGuestId, HttpServletResponse response) {
    final ResponseCookie newGuestCookie = ResponseCookie.from("guest_tracking_id", newGuestId)//
        .path("/")//
        .httpOnly(true)//
        .secure(true)//
        .sameSite("Lax")//
        .maxAge(Duration.ofMinutes(5L))//
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newGuestCookie.toString());
    commonReader.recordUserActivity(newGuestId);

    return ResponseApi.ok(true);
  }

  @CacheEvict(cacheNames = "getUserInfo", key = "#userId",
      condition = "#userId != null && !#userId.equals('')")
  public void expireToken(String userId, HttpServletResponse response) {
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();
    final ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    commonReader.removeUserActivity(userId);
  }

  public void recordUserActivity(String id) {
    commonReader.recordUserActivity(id);
  }

  public void removeUserActivity(String id) {
    commonReader.removeUserActivity(id);
  }

  public void makeToken(String userId, HttpServletResponse response) {
    makeAccessToken(userId, response);
    makeRefreshToken(userId, response);
  }

  public void makeAccessToken(String userId, HttpServletResponse response) {
    final String accessToken = tokenUtil.createToken(userId, 1L);
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken)
        .path("/").httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofHours(1L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
  }

  public void makeRefreshToken(String userId, HttpServletResponse response) {
    final String refreshToken = tokenUtil.createToken(userId, 8760L);
    final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    final ResponseCookie refreshCookie =
        ResponseCookie.from(REFRESH_TOKEN.getValue(), refreshToken).path("/").httpOnly(true)
            .secure(true).sameSite("Lax").maxAge(Duration.between(now, now.plusYears(1L))).build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  public String makeNewAccessToken(HttpServletRequest request) {
    final String refreshToken = getRefreshToken(request);

    if (refreshToken != null) {
      String userId = tokenUtil.getTokenInfo(refreshToken).getSubject();
      User user = readUserRepository.findLoginInfoById(userId);

      return user != null ? tokenUtil.createToken(userId, 1L) : null;
    }

    return null;
  }
}
