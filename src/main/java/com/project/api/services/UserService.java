package com.project.api.services;

import static com.project.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.project.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.utils.TokenUtil;
import com.project.api.dtos.Login;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final TokenUtil tokenUtil;

  @Async("appAsync")
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    return ResponseApi.completedOk(true);
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<Boolean>> login(Login login, HttpServletRequest request,
      HttpServletResponse response) {
    final String authToken = tokenUtil.createToken(login.getLoginId(), 1L);
    final String refreshToken = tokenUtil.createToken(login.getLoginId(), 8760L);
    final LocalDateTime now = LocalDateTime.now();
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), authToken)
        .path("/").httpOnly(true).secure(true).sameSite("Lax").maxAge(3600).build();
    final ResponseCookie refreshCookie =
        ResponseCookie.from(REFRESH_TOKEN.getValue(), refreshToken).path("/").httpOnly(true)
            .secure(true).sameSite("Lax").maxAge(Duration.between(now, now.plusYears(1L))).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseApi.completedOk(true);
  }

}
