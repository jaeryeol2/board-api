package com.board.api.services.read;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import java.time.LocalDateTime;
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
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadUserRepository;
import com.board.api.services.internal.InUserService;
import com.board.api.services.write.WriteUserService;
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

  private final InUserService inUserService;
  private final WriteUserService writeUserService;

  private final ReadUserRepository readUserRepository;


  @Async
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    return ResponseApi.asyncOk(() -> {
      String userId = inUserService.getUserId(request);
      Optional<User> user = Optional.ofNullable(inUserService.getUserInfo(userId));

      if (user.isEmpty()) {
        writeUserService.expireToken(response);
      }

      return true;
    });
  }

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
        writeUserService.makeToken(login.getUserId(), response);

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
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    return ResponseApi.asyncOk(() -> {
      final String refreshToken = inUserService.getRefreshToken(request);
      final String userId = tokenUtil.getTokenInfo(refreshToken).getSubject();
      final Optional<User> user = Optional.ofNullable(readUserRepository.findLoginInfoById(userId));
      boolean result = true;

      if (user.isPresent()) {
        final String accessToken = tokenUtil.createToken(userId, 1L);
        final ResponseCookie accessCookie =
            ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken).path("/").httpOnly(true)
                .secure(true).sameSite("Lax").maxAge(3600).build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
      } else {
        writeUserService.expireToken(response);
        result = false;
      }

      return result;
    });
  }

  @Async
  public CompletableFuture<ResponseApi<String>> getUserId(HttpServletRequest request) {
    final String token = inUserService.getAccessToken(request);
    return ResponseApi.completedOk(tokenUtil.getTokenInfo(token).getSubject());
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    final String userId = inUserService.getUserId(request);
    return ResponseApi.asyncOk(() -> inUserService.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return ResponseApi.asyncOk(() -> inUserService.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<PersonalUser>> getPersonalUserInfo(
      HttpServletRequest request) {
    return ResponseApi.asyncOk(() -> {
      final String userId = inUserService.getUserId(request);
      log.info(userId);
      return null;
    });
  }



}
