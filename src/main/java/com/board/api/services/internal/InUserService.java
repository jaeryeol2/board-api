package com.board.api.services.internal;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InUserService {
  private final TokenUtil tokenUtil;
  private final ReadUserRepository readUserRepository;

  @Cacheable(cacheNames = "getUserInfo", key = "#userId", unless = "#result == null")
  public User getUserInfo(String userId) {
    return readUserRepository.findUserById(userId);
  }

  public String getUserId(HttpServletRequest request) {
    final String token = Optional.ofNullable(this.getAccessToken(request))//
        .orElse(this.getRefreshToken(request));
    return Optional.ofNullable(token).map(t -> tokenUtil.getTokenInfo(t).getSubject()).orElse(null);
  }

  public boolean checkToken(HttpServletRequest request) {
    final String token = Optional.ofNullable(this.getAccessToken(request))//
        .orElse(this.getRefreshToken(request));

    return !tokenUtil.isTokenExpired(token);
  }

  public String getAccessToken(HttpServletRequest request) {
    Optional<String> optional = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));

    if (optional.isPresent()) {
      final String token = optional.get().replaceAll("^(?i)Bearer\\s+", "");
      return !tokenUtil.isTokenExpired(token) ? token : null;
    }

    return null;
  }

  public String getRefreshToken(HttpServletRequest request) {
    final Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);
    final String token = Arrays.asList(cookies).stream()
        .filter(
            cookie -> cookie != null && REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName()))
        .findFirst().orElseGet(() -> {
          final Cookie cookie = new Cookie(REFRESH_TOKEN.getValue(), null);
          cookie.setSecure(true);

          return cookie;
        }).getValue();

    return !tokenUtil.isTokenExpired(token) ? token : null;
  }

  @Cacheable(cacheNames = "getGuestId", key = "#newGuestId")
  public ResponseApi<Boolean> setGuest(final String newGuestId, HttpServletResponse response) {
    final ResponseCookie newGuestCookie = ResponseCookie.from("guest_tracking_id", newGuestId)//
        .path("/")//
        .httpOnly(true)//
        .secure(true)//
        .sameSite("Lax")//
        .maxAge(Duration.ofMinutes(5L))//
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newGuestCookie.toString());

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
  }

  public void makeToken(String userId, HttpServletResponse response) {
    final String accessToken = tokenUtil.createToken(userId, 1L);
    final String refreshToken = tokenUtil.createToken(userId, 8760L);
    final LocalDateTime now = LocalDateTime.now();
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken)
        .path("/").httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofHours(1L)).build();
    final ResponseCookie refreshCookie =
        ResponseCookie.from(REFRESH_TOKEN.getValue(), refreshToken).path("/").httpOnly(true)
            .secure(true).sameSite("Lax").maxAge(Duration.between(now, now.plusYears(1L))).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  public String makeNewAccessToken(HttpServletRequest request) {
    final String refreshToken = this.getRefreshToken(request);
    final String userId = tokenUtil.getTokenInfo(refreshToken).getSubject();
    final Optional<User> user = Optional.ofNullable(readUserRepository.findLoginInfoById(userId));

    if (user.isPresent()) {
      return tokenUtil.createToken(userId, 1L);
    }

    return null;
  }

}
