package com.board.api.zconfig.entrypoint;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.board.api.common.base.ResponseApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 403 Forbidden 에러 핸들러 인증은 되었으나 해당 자원에 대한 권한이 부족한 경우 호출됨
 */
@Component
@RequiredArgsConstructor
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ResponseApi<Object> fail =
        ResponseApi.fail(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "해당 자원에 대한 접근 권한이 없습니다.");

    response.getWriter().write(objectMapper.writeValueAsString(fail));
  }
}
