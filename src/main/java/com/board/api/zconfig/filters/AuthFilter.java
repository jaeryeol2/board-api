package com.board.api.zconfig.filters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
import com.board.api.services.internal.InUserService;
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
  private final InUserService inUserService;
  private final AuthEntryPoint authEntryPoint;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final String path = request.getRequestURI();
    final String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
        .filter(item -> !item.isBlank() && !"unknown".equals(item))
        .map(item -> item.split(",")[0].trim())//
        .orElse(request.getRemoteAddr());
    final String token = Optional.ofNullable(inUserService.getAccessToken(request))//
        .orElse(inUserService.getRefreshToken(request));

    try {
      if (StringUtils.hasText(token) && !tokenUtil.isTokenExpired(token)) {
        final String userId = tokenUtil.getTokenInfo(token).getSubject();
        final Optional<User> user = Optional.ofNullable(inUserService.getUserInfo(userId));

        if (user.isPresent()) {
          final GrantedAuthority authority = new SimpleGrantedAuthority(user.get().getUserRole());
          final List<GrantedAuthority> authorities = List.of(authority);
          final DefaultAuthority authentication =
              new DefaultAuthority(user.get(), token, authorities);

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception e) {
      log.warn("Authentication failed for IP: {}, Path: {}, Error: {}", ip, path, e.getMessage());
      SecurityContextHolder.clearContext();
      authEntryPoint.commence(request, response,
          new AuthenticationException(e.getMessage(), e.getCause()) {});

      return;
    }

    log.info("api path : {}, client ip : {}", path, ip);

    filterChain.doFilter(request, response);
  }

}
