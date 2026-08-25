package com.board.api.services.user;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.request.Signup;
import com.board.api.dtos.request.UserPasswordUpdate;
import com.board.api.dtos.request.UserProfileUpdate;
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
  private final TaskExecutor appAsync;

  private final UserReader userReader;
  private final UserWriter userWriter;

  @Async
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    String userId = userReader.getUserId(request);
    User user = userReader.getUserInfo(userId);

    if (user == null) {
      userReader.expireToken(userId, response);
    }

    return ResponseApi.completedOk(user != null);
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> login(String guestId, Login login,
      HttpServletRequest request, HttpServletResponse response) {
    final User user = userReader.getLoginUserInfo(login.getUserId());

    if (user == null) {
      throw new BusinessException("존재하지 않는 사용자입니다.");
    }

    final boolean matched = passwordEncoder.matches(login.getPassword(), user.getPassword());

    if (!matched) {
      throw new BusinessException("비밀번호가 일치하지 않습니다.");
    }

    userReader.makeToken(login.getUserId(), response);

    CompletableFuture.runAsync(() -> {
      boolean isLoginUpdate = userWriter.updateLastLoginDate(login.getUserId());

      if (!isLoginUpdate) {
        log.warn("user last login date update fail. id : {}, date : {}", login.getUserId(),
            OffsetDateTime.now(ZoneOffset.UTC));
      }
    }, appAsync);

    ResponseCookie exppireGuestId = ResponseCookie.from("guest_tracking_id", "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, exppireGuestId.toString());

    userWriter.removeTrafficInfoCache(guestId);
    userReader.recordUserActivity(login.getUserId());

    return ResponseApi.completedOk(true);
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    final String token = userReader.makeNewAccessToken(request);

    if (token != null) {
      final ResponseCookie accessCookie =
          ResponseCookie.from(ACCESS_TOKEN.getValue(), token).path("/").httpOnly(true).secure(true)
              .sameSite("Lax").maxAge(Duration.ofHours(1L)).build();
      response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    return ResponseApi.completedOk(token != null);
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
    final PersonalUser loginUser = userReader.getLoginUserInfo(userId);

    return ResponseApi.completedOk(loginUser);
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> signup(Signup join) {
    return ResponseApi.completedOk(HttpStatus.CREATED, "회원가입이 완료되었습니다.", userWriter.signup(join));
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> updateProfile(HttpServletRequest request,
      UserProfileUpdate updateDto) {
    final String userId = userReader.getUserId(request);
    if (userId == null) {
      throw new BusinessException("인증 정보가 유효하지 않습니다.");
    }
    final boolean updated = userWriter.updateProfile(userId, updateDto);
    return ResponseApi.completedOk(HttpStatus.OK, "프로필이 수정되었습니다.", updated);
  }

  @Async
  @Transactional
  public CompletableFuture<ResponseApi<Boolean>> updatePassword(HttpServletRequest request,
      UserPasswordUpdate updateDto) {
    final String userId = userReader.getUserId(request);
    if (userId == null) {
      throw new BusinessException("인증 정보가 유효하지 않습니다.");
    }
    final boolean updated = userWriter.updatePassword(userId, updateDto.getCurrentPassword(),
        updateDto.getNewPassword());
    return ResponseApi.completedOk(HttpStatus.OK, "비밀번호가 변경되었습니다.", updated);
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
