package com.board.api.services.user;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.request.Join;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final TokenUtil tokenUtil;
  private final PasswordEncoder passwordEncoder;

  private final UserReader userReader;
  private final UserWriter userWriter;

  @Async
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    String userId = userReader.getUserId(request);
    Optional<User> user = Optional.ofNullable(userReader.getUserInfo(userId));

    if (user.isEmpty()) {
      userReader.expireToken(userId, response);
    }

    return ResponseApi.completedOk(user.isPresent());
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> login(String guestId, Login login,
      HttpServletRequest request, HttpServletResponse response) {
    final Optional<User> checker =
        Optional.ofNullable(userReader.getLoginUserInfo(login.getUserId()));

    if (!checker.isPresent()) {
      throw new BusinessException("존재하지 않는 사용자입니다.");
    }

    final boolean matched =
        passwordEncoder.matches(login.getPassword(), checker.get().getPassword());

    if (!matched) {
      throw new BusinessException("비밀번호가 일치하지 않습니다.");
    }

    try {
      userReader.makeToken(login.getUserId(), response);
      boolean isLoginUpdate = userWriter.updateLastLoginDate(login.getUserId());

      if (!isLoginUpdate) {
        throw new BusinessException("last login date update fail.");
      }

      ResponseCookie exppireGuestId = ResponseCookie.from("guest_tracking_id", "").path("/")
          .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

      response.addHeader(HttpHeaders.SET_COOKIE, exppireGuestId.toString());
    } catch (Exception e) {
      log.warn("user last login date update fail. id : {}, date : {}", login.getUserId(),
          LocalDateTime.now());
    }

    userWriter.removeGuestIdCache(guestId);

    return ResponseApi.completedOk(true);
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    final Optional<String> optional = Optional.ofNullable(userReader.makeNewAccessToken(request));

    if (optional.isPresent()) {
      final ResponseCookie accessCookie =
          ResponseCookie.from(ACCESS_TOKEN.getValue(), optional.get()).path("/").httpOnly(true)
              .secure(true).sameSite("Lax").maxAge(Duration.ofHours(1L)).build();
      response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    return ResponseApi.completedOk(optional.isPresent());
  }

  @Async
  public CompletableFuture<ResponseApi<String>> getUserId(HttpServletRequest request) {
    final String token = userReader.getAccessToken(request);
    return ResponseApi.completedOk(tokenUtil.getTokenInfo(token).getSubject());
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    final String userId = userReader.getUserId(request);
    return ResponseApi.completedOk(userReader.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return ResponseApi.completedOk(userReader.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<PersonalUser>> getPersonalUserInfo(
      HttpServletRequest request) {
    final String userId = userReader.getUserId(request);
    log.info(userId);

    return ResponseApi.completedOk(null);
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> join(Join join) {
    return ResponseApi.completedOk(userWriter.join(join));
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      String userId = userReader.getUserId(request);
      userReader.expireToken(userId, response);
    } catch (Exception e) {
      log.warn("expire token error : ", e);
    }

    return ResponseApi.completedOk(true);
  }

}
