package com.board.api.services.common;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.board.api.common.base.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 기반 남용 방지 처리 (로그인 실패 제한, 가입 횟수 제한, 이미지 업로더 기록, 조회수 중복 방지).
 * 제한 기능은 Redis 장애 시 기존 동작을 막지 않도록 통과(fail-open)시키고,
 * 이미지 삭제 권한 확인만 장애 시 거부(fail-closed)합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestGuard {
  private final StringRedisTemplate redisTemplate;

  private static final String LOGIN_FAIL_USER_KEY = "login-fail:user:";
  private static final String LOGIN_FAIL_IP_KEY = "login-fail:ip:";
  private static final String SIGNUP_IP_KEY = "signup:ip:";
  private static final String IMAGE_OWNER_KEY = "board-image-owner:";
  private static final String VIEW_KEY = "board-view:";

  private static final long LOGIN_FAIL_LIMIT_PER_USER = 5L;
  private static final long LOGIN_FAIL_LIMIT_PER_IP = 20L;
  private static final long SIGNUP_LIMIT_PER_IP = 10L;
  private static final Duration LOGIN_FAIL_WINDOW = Duration.ofMinutes(15L);
  private static final Duration SIGNUP_WINDOW = Duration.ofHours(1L);
  // 글 작성 화면에서 업로드 후 삭제(작성 취소·본문에서 제거)까지 허용하는 시간
  private static final Duration IMAGE_OWNER_TTL = Duration.ofDays(1L);
  private static final Duration VIEW_WINDOW = Duration.ofHours(1L);

  private static final String TOO_MANY_LOGIN = "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.";
  private static final String TOO_MANY_SIGNUP = "회원가입 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.";

  public void checkLoginAllowed(final String userId, final String ip) {
    if (exceeds(LOGIN_FAIL_USER_KEY + userId, LOGIN_FAIL_LIMIT_PER_USER)
        || exceeds(LOGIN_FAIL_IP_KEY + ip, LOGIN_FAIL_LIMIT_PER_IP)) {
      throw new BusinessException(TOO_MANY_LOGIN, HttpStatus.TOO_MANY_REQUESTS);
    }
  }

  public void recordLoginFailure(final String userId, final String ip) {
    increment(LOGIN_FAIL_USER_KEY + userId, LOGIN_FAIL_WINDOW);
    increment(LOGIN_FAIL_IP_KEY + ip, LOGIN_FAIL_WINDOW);
  }

  public void clearLoginFailure(final String userId) {
    try {
      redisTemplate.delete(LOGIN_FAIL_USER_KEY + userId);
    } catch (Exception e) {
      log.error("clearLoginFailure error : {}", e.getMessage());
    }
  }

  public void checkSignupAllowed(final String ip) {
    if (increment(SIGNUP_IP_KEY + ip, SIGNUP_WINDOW) > SIGNUP_LIMIT_PER_IP) {
      throw new BusinessException(TOO_MANY_SIGNUP, HttpStatus.TOO_MANY_REQUESTS);
    }
  }

  public void markImageOwner(final String imageKey, final String userId) {
    try {
      redisTemplate.opsForValue().set(IMAGE_OWNER_KEY + imageKey, userId, IMAGE_OWNER_TTL);
    } catch (Exception e) {
      log.error("markImageOwner error : {}", e.getMessage());
    }
  }

  public boolean isImageOwner(final String imageKey, final String userId) {
    try {
      return userId != null
          && userId.equals(redisTemplate.opsForValue().get(IMAGE_OWNER_KEY + imageKey));
    } catch (Exception e) {
      log.error("isImageOwner error : {}", e.getMessage());
      return false;
    }
  }

  /**
   * 같은 사용자(또는 게스트·IP)의 동일 게시글 조회를 VIEW_WINDOW 동안 1회만 인정합니다.
   *
   * @return 조회수를 올려야 하면 true
   */
  public boolean markViewed(final long boardId, final String viewerId) {
    try {
      return Boolean.TRUE.equals(redisTemplate.opsForValue()
          .setIfAbsent(VIEW_KEY + boardId + ":" + viewerId, "1", VIEW_WINDOW));
    } catch (Exception e) {
      log.error("markViewed error : {}", e.getMessage());
      return true;
    }
  }

  private boolean exceeds(final String key, final long limit) {
    try {
      final String count = redisTemplate.opsForValue().get(key);
      return count != null && Long.parseLong(count) >= limit;
    } catch (Exception e) {
      log.error("rate limit check error : {}", e.getMessage());
      return false;
    }
  }

  private long increment(final String key, final Duration window) {
    try {
      final Long count = redisTemplate.opsForValue().increment(key);
      if (count != null && count == 1L) {
        redisTemplate.expire(key, window);
      }
      return count != null ? count : 0L;
    } catch (Exception e) {
      log.error("rate limit increment error : {}", e.getMessage());
      return 0L;
    }
  }
}
