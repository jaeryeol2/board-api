package com.board.api.services.read;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadUserRepository;
import com.board.api.services.InternalService;
import com.board.api.services.LookupService;
import com.board.api.services.write.WriteUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadUserService {
  private final TokenUtil tokenUtil;
  private final PasswordEncoder passwordEncoder;

  private final InternalService internalService;
  private final LookupService lookupService;
  private final WriteUserService writeUserService;

  private final ReadUserRepository readUserRepository;

  @Async
  public CompletableFuture<ResponseApi<Boolean>> login(final Login login,
      HttpServletRequest request, HttpServletResponse response) {
    return ResponseApi.asyncOk(() -> {
      final Optional<User> checker =
          Optional.ofNullable(readUserRepository.findLoginInfoById(login.getUserId()));
      if (!checker.isPresent()) {
        throw new BusinessException("존재하지 않는 사용자입니다.");
      }

      if (passwordEncoder.matches(login.getPassword(), checker.get().getPassword())) {
        final String accessToken = tokenUtil.createToken(login.getUserId(), 1L);
        final String refreshToken = tokenUtil.createToken(login.getUserId(), 8760L);
        final LocalDateTime now = LocalDateTime.now();
        final ResponseCookie accessCookie =
            ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken).path("/").httpOnly(true)
                .secure(true).sameSite("Lax").maxAge(3600).build();
        final ResponseCookie refreshCookie = ResponseCookie
            .from(REFRESH_TOKEN.getValue(), refreshToken).path("/").httpOnly(true).secure(true)
            .sameSite("Lax").maxAge(Duration.between(now, now.plusYears(1L))).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        try {
          ResponseApi<Boolean> loginUpdate =
              writeUserService.updateLastLoginDate(login.getUserId());

          if (Boolean.FALSE.equals(loginUpdate.getData())) {
            throw new BusinessException("last login date update fail.");
          }

        } catch (Exception e) {
          log.warn("user last login date update fail. id : {}, date : {}", login.getUserId(),
              LocalDateTime.now());
        }

        return true;
      }

      throw new BusinessException("비밀번호가 일치하지 않습니다.");
    });
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletResponse response) {
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();
    final ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseApi.completedOk(true);
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    final Cookie refreshToken = Arrays.asList(request.getCookies()).stream()
        .filter(cookie -> REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName())).findFirst()
        .orElseGet(() -> new Cookie(REFRESH_TOKEN.getValue(), ""));
    final String loginId = tokenUtil.getTokenInfo(refreshToken.getValue()).getSubject();
    final String accessToken = tokenUtil.createToken(loginId, 1L);
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken)
        .path("/").httpOnly(true).secure(true).sameSite("Lax").maxAge(3600).build();
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    return ResponseApi.completedOk(true);

  }

  @Async
  public CompletableFuture<ResponseApi<String>> getUserId(HttpServletRequest request) {
    final String token = internalService.getAccessToken(request);
    return ResponseApi.completedOk(tokenUtil.getTokenInfo(token).getSubject());
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    final String userId =
        tokenUtil.getTokenInfo(internalService.getAccessToken(request)).getSubject();
    return lookupService.getUserInfo(userId);
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return lookupService.getUserInfo(userId);
  }



}
