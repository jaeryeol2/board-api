package com.board.api.services;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
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
public class InternalService {
  private final TokenUtil tokenUtil;
  private final ReadUserRepository readUserRepository;

  @Cacheable(cacheNames = "getUserInfo", key = "#userId")
  public ResponseApi<User> getUserInfo(String userId) {
    return ResponseApi.ok(readUserRepository.findUserById(userId));
  }

  public ResponseApi<String> getUserId(HttpServletRequest request) {
    final String token = this.getAccessToken(request);
    return ResponseApi.ok(tokenUtil.getTokenInfo(token).getSubject());
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
        .findFirst().orElseGet(() -> new Cookie(REFRESH_TOKEN.getValue(), null)).getValue();

    return !tokenUtil.isTokenExpired(token) ? token : null;
  }

  public void expireToken(HttpServletResponse response) {
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();
    final ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }
}
