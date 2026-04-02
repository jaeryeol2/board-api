package com.project.api.services;

import static com.project.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.project.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.condition.Case;
import com.project.api.common.utils.TokenUtil;
import com.project.api.constants.UserRole;
import com.project.api.dtos.request.Join;
import com.project.api.dtos.request.Login;
import com.project.api.dtos.response.User;
import com.project.api.entities.AppUser;
import com.project.api.repositories.read.ReadUserRepository;
import com.project.api.repositories.write.WriteUserRepository;
import jakarta.servlet.http.Cookie;
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

  private final CommonService commonService;

  private final TransactionTemplate transactionTemplate;
  private final WriteUserRepository writeUserRepository;
  private final ReadUserRepository readUserRepository;


  private String getAccessToken(HttpServletRequest request) {
    Optional<String> optional = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));

    if (optional.isPresent()) {
      return optional.get().replaceAll("^(?i)Bearer\\s+", "");
    }

    return null;
  }

  private String getRefreshToken(HttpServletRequest request) {
    final Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);
    return Arrays.asList(cookies).stream()
        .filter(
            cookie -> cookie != null && REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName()))
        .findFirst().orElseGet(() -> new Cookie(REFRESH_TOKEN.getValue(), null)).getValue();
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    final Optional<String> refreshToken = Optional.ofNullable(getRefreshToken(request));
    return ResponseApi.completedOk(refreshToken.isPresent());
  }

  @Async("appAsync")
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

        transactionTemplate.execute(status -> {
          writeUserRepository.updateLastLoginDate(login.getUserId());
          return status;
        });

        return true;
      }

      throw new BusinessException("비밀번호가 일치하지 않습니다.");
    });
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletResponse response) {
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();
    final ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseApi.completedOk(true);
  }

  @Async("appAsync")
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

  @Async("appAsync")
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    final String userId = tokenUtil.getTokenInfo(getAccessToken(request)).getSubject();
    return commonService.getUserInfo(userId);
  }

  @Async("appAsync")
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return commonService.getUserInfo(userId);
  }

  @Transactional
  public ResponseApi<Boolean> join(Join join) {
    try {
      final AppUser appUser = writeUserRepository.save(AppUser.builder()//
          .userId(join.getUserId())//
          .password(join.getPassword())//
          .userName(join.getUserName())//
          .email(join.getEmail())//
          .userRole(UserRole.USER_GENERAL.getCode())//
          .build());

      return Optional.of(!ObjectUtils.isEmpty(appUser))//
          .filter(bool -> bool)//
          .map(bool -> ResponseApi.ok(HttpStatus.CREATED, "success", bool))//
          .orElse(ResponseApi.fail("fail", false));
    } catch (Exception e) {
      log.error("join error : ", e);
      final int firstIndex = 0;
      final String message = e.getMessage();
      final boolean isDuplicate = e instanceof DataIntegrityViolationException;
      final String[] resMessage = new String[] {"회원가입에 실패 했습니다."};

      Case.when(isDuplicate && message.contains("user_id"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 아이디입니다.")//
          .when(isDuplicate && message.contains("email"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 이메일입니다.")//
          .when(isDuplicate)//
          .then(() -> resMessage[firstIndex] = "중복되는 데이터가 있습니다.")//
          .execute();

      throw new BusinessException(resMessage[firstIndex]);
    }

  }

}
