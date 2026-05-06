package com.board.api.services;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheEvict;
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
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.condition.Case;
import com.board.api.common.utils.TokenUtil;
import com.board.api.constants.UserRole;
import com.board.api.dtos.request.Join;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import com.board.api.entities.AppUser;
import com.board.api.repositories.read.ReadUserRepository;
import com.board.api.repositories.write.WriteUserRepository;
import com.board.api.services.internal.InUserService;
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
  private final TransactionTemplate writeTransaction;

  private final InUserService inUserService;

  private final ReadUserRepository readUserRepository;
  private final WriteUserRepository writeUserRepository;


  @Async
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    String userId = inUserService.getUserId(request);
    Optional<User> user = Optional.ofNullable(inUserService.getUserInfo(userId));

    if (user.isEmpty()) {
      inUserService.expireToken(userId, response);
    }

    return ResponseApi.completedOk(user.isPresent());
  }

  @Async
  @CacheEvict(cacheNames = "getGuestId", key = "#guestId")
  public CompletableFuture<ResponseApi<Boolean>> login(String guestId, Login login,
      HttpServletRequest request, HttpServletResponse response) {
    final Optional<User> checker =
        Optional.ofNullable(readUserRepository.findLoginInfoById(login.getUserId()));

    if (!checker.isPresent()) {
      throw new BusinessException("존재하지 않는 사용자입니다.");
    }

    final boolean matched =
        passwordEncoder.matches(login.getPassword(), checker.get().getPassword());

    if (!matched) {
      throw new BusinessException("비밀번호가 일치하지 않습니다.");
    }

    try {
      inUserService.makeToken(login.getUserId(), response);
      boolean isLoginUpdate = writeTransaction
          .execute(status -> writeUserRepository.updateLastLoginDate(login.getUserId()) > 0);

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

    return ResponseApi.completedOk(true);
  }

  @Async
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    final Optional<String> optional =
        Optional.ofNullable(inUserService.makeNewAccessToken(request));

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
    final String token = inUserService.getAccessToken(request);
    return ResponseApi.completedOk(tokenUtil.getTokenInfo(token).getSubject());
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    final String userId = inUserService.getUserId(request);
    return ResponseApi.completedOk(inUserService.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<User>> getUserInfo(String userId) {
    return ResponseApi.completedOk(inUserService.getUserInfo(userId));
  }

  @Async
  public CompletableFuture<ResponseApi<PersonalUser>> getPersonalUserInfo(
      HttpServletRequest request) {
    final String userId = inUserService.getUserId(request);
    log.info(userId);

    return ResponseApi.completedOk(null);
  }

  @Transactional
  public ResponseApi<Boolean> join(Join join) {
    try {
      final AppUser appUser = writeUserRepository.save(AppUser.builder()//
          .userId(join.getUserId())//
          .password(passwordEncoder.encode(join.getPassword()))//
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

  @Async
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      String userId = inUserService.getUserId(request);
      inUserService.expireToken(userId, response);
    } catch (Exception e) {
      log.warn("expire token error : ", e);
    }

    return ResponseApi.completedOk(true);
  }

}
