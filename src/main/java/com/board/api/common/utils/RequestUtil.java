package com.board.api.common.utils;

import static com.board.api.constants.GlobalConstant.X_FORWARDED_FOR;
import static com.board.api.constants.GlobalConstant.X_GUEST_ID;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 요청 헤더 공통 추출 유틸
 */
public final class RequestUtil {
  // 게스트 ID는 서버가 UUID로 발급하므로 UUID 형식만 허용합니다.
  private static final Pattern UUID_PATTERN = Pattern
      .compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private RequestUtil() {}

  public static String getClientIp(final HttpServletRequest request) {
    final String ip = request.getHeader(X_FORWARDED_FOR.getValue());

    if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
      return ip.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }

  /**
   * X-Guest-Id 헤더 값이 UUID 형식일 때만 반환합니다. (동시접속자 수 조작 방지)
   */
  public static String getGuestId(final HttpServletRequest request) {
    final String guestId = request.getHeader(X_GUEST_ID.getValue());
    return isValidGuestId(guestId) ? guestId : null;
  }

  public static boolean isValidGuestId(final String guestId) {
    return guestId != null && UUID_PATTERN.matcher(guestId).matches();
  }
}
