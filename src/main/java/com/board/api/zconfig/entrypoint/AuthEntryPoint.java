package com.board.api.zconfig.entrypoint;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.board.api.common.base.ResponseApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    setResponse(response, authException);
  }

  private void setResponse(HttpServletResponse response, AuthenticationException e)
      throws IOException {
    // 1. Content-Type 및 인코딩 설정 (한글 깨짐 방지)
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // 2. 상태 코드 설정
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    // 3. 응답으로 내보낼 JSON 데이터 생성
    ResponseApi<Boolean> fail = ResponseApi.fail(HttpStatus.UNAUTHORIZED, e.getMessage());

    // 4. 응답 Body에 직접 JSON 문자열 쓰기
    response.getWriter().print(this.objectMapper.writeValueAsString(fail));
  }

}
