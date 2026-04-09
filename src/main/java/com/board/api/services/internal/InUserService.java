package com.board.api.services.internal;

import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    return tokenUtil.getTokenInfo(token).getSubject();
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
        .findFirst().orElse(new Cookie(REFRESH_TOKEN.getValue(), null)).getValue();

    return !tokenUtil.isTokenExpired(token) ? token : null;
  }

}
