package com.board.api.zconfig.filters;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.board.api.common.utils.RequestUtil;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.response.User;
import com.board.api.services.user.UserReader;
import com.board.api.zconfig.authority.DefaultAuthority;
import com.board.api.zconfig.entrypoint.AuthEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
  private final TokenUtil tokenUtil;
  private final UserReader userReader;
  private final AuthEntryPoint authEntryPoint;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final String path = request.getRequestURI();
    final String method = request.getMethod();
    final String ip = RequestUtil.getClientIp(request);

    try {
      authenticateAndTrack(request, response, path);
    } catch (Exception e) {
      log.warn("Authentication failed for IP: {}, Path: {}, Error: {}", ip, path, e.getMessage());

      SecurityContextHolder.clearContext();
      authEntryPoint.commence(request, response,
          new AuthenticationException(e.getMessage(), e.getCause()) {});

      return;
    }

    log.info("api path : {}, method : {}, client ip : {}", path, method, ip);

    filterChain.doFilter(request, response);
  }

  private void authenticateAndTrack(HttpServletRequest request, HttpServletResponse response,
      String path) {
    final String accessToken = userReader.getAccessToken(request);
    final String refreshToken = userReader.getRefreshToken(request);

    if (tryAuthenticateAccessToken(request, accessToken)) {
      return;
    }

    if (tryReissueAndAuthenticate(request, path, refreshToken)) {
      return;
    }

    handleGuest(request, response, refreshToken);
  }

  private boolean tryAuthenticateAccessToken(HttpServletRequest request, String accessToken) {
    if (!StringUtils.hasText(accessToken) || tokenUtil.isTokenExpired(accessToken)) {
      return false;
    }

    final String userId = tokenUtil.getTokenInfo(accessToken).getSubject();
    if (processAuthorityAndResult(request, accessToken, userId)) {
      userReader.recordUserActivity(userId);
      return true;
    }
    return false;
  }

  private boolean tryReissueAndAuthenticate(HttpServletRequest request, String path,
      String refreshToken) {
    if (!"/api/auth/access".equals(path) || !StringUtils.hasText(refreshToken)
        || tokenUtil.isTokenExpired(refreshToken)) {
      return false;
    }

    final String newAccessToken = userReader.makeNewAccessToken(request);
    if (!StringUtils.hasText(newAccessToken)) {
      return false;
    }

    final String userId = tokenUtil.getTokenInfo(newAccessToken).getSubject();
    if (processAuthorityAndResult(request, newAccessToken, userId)) {
      userReader.recordUserActivity(userId);
      return true;
    }
    return false;
  }

  private void handleGuest(HttpServletRequest request, HttpServletResponse response,
      String refreshToken) {
    // 형식이 올바른(서버가 발급한 UUID) 게스트 ID만 접속자로 기록합니다.
    final String guestId = RequestUtil.getGuestId(request);
    if (guestId != null) {
      userReader.recordUserActivity(guestId);
      return;
    }

    final String newGuestId = UUID.randomUUID().toString();
    userReader.setGuest(newGuestId, response);
    expireInvalidTokens(request, refreshToken, response);
  }

  /**
   * refresh 쿠키가 있지만 무효(만료·위조)일 때만 토큰 쿠키를 삭제합니다.
   * access 토큰만 만료되고 refresh가 유효한 경우에는 재발급이 가능하도록 쿠키를 유지합니다.
   */
  private void expireInvalidTokens(HttpServletRequest request, String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null && userReader.hasRefreshTokenCookie(request)) {
      userReader.expireToken(null, response);
    }
  }

  private boolean processAuthorityAndResult(HttpServletRequest request, final String accessToken,
      final String userId) {
    final User user = userReader.getUserInfo(userId);

    if (user == null) {
      return false;
    }

    final GrantedAuthority authority = new SimpleGrantedAuthority(user.getUserRole());
    final List<GrantedAuthority> authorities = List.of(authority);
    final DefaultAuthority authentication = new DefaultAuthority(user, accessToken, authorities);

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    return true;
  }

}
