package com.project.api.zconfig.filters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.project.api.services.UserService;
import com.project.api.zconfig.authority.DefaultAuthority;
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
  private final UserService userService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final String path = request.getRequestURI();
    boolean isLogin = false;

    log.info("api path : {}", path);

    try {
      isLogin = userService.authCheck(request, response).get().getData();
    } catch (InterruptedException | ExecutionException e) {
      if (Thread.interrupted()) {
        Thread.currentThread().interrupt();
      }

      log.error("auth check error.", e);
    }

    if (isLogin) {
      final String token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
          .orElseGet(() -> "").replaceAll("^(?i)Bearer\\s+", "");
      GrantedAuthority authority = new SimpleGrantedAuthority("USER");
      List<GrantedAuthority> authorities = List.of(authority);
      DefaultAuthority authentication = new DefaultAuthority("user", token, authorities);

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    log.info("isLogin : {}", isLogin);

    filterChain.doFilter(request, response);
  }

}
