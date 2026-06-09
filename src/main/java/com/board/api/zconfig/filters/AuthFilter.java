package com.board.api.zconfig.filters;

import static com.board.api.constants.GlobalConstant.X_FORWARDED_FOR;
import static com.board.api.constants.GlobalConstant.X_GUEST_ID;
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
    final String ip = this.extractClientIp(request);
    final String accessToken = this.getToken(request);
    boolean isGuest = true;

    try {
      if (StringUtils.hasText(accessToken) && !tokenUtil.isTokenExpired(accessToken)) {
        final String userId = tokenUtil.getTokenInfo(accessToken).getSubject();
        final User user = userReader.getUserInfo(userId);

        if (user != null) {
          final GrantedAuthority authority = new SimpleGrantedAuthority(user.getUserRole());
          final List<GrantedAuthority> authorities = List.of(authority);
          final DefaultAuthority authentication =
              new DefaultAuthority(user, accessToken, authorities);

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);

          isGuest = false;
        }
      }

      if (isGuest && !StringUtils.hasText(request.getHeader(X_GUEST_ID.getValue()))) {
        String newGuestId = UUID.randomUUID().toString();
        userReader.setGuest(newGuestId, response);
      }
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

  private String getToken(HttpServletRequest request) {
    String token = userReader.getAccessToken(request);

    if (!StringUtils.hasText(token)) {
      token = userReader.makeNewAccessToken(request);
    }

    return token;
  }

  private String extractClientIp(HttpServletRequest request) {
    String ip = request.getHeader(X_FORWARDED_FOR.getValue());

    if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
      return ip.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }

}
