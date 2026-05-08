package com.board.api.zconfig.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.board.api.services.user.UserReader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InternalConfig {
  private final UserReader userReader;

  @Bean
  public RestClient internal(RestClient.Builder builder) {
    return builder.baseUrl("")//
        .requestInterceptor((request, body, execution) -> {
          final ServletRequestAttributes attributes =
              (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

          if (attributes != null) {
            HttpServletRequest servletRequest = attributes.getRequest();
            String token = userReader.getAccessToken(servletRequest);

            // 2. API를 쏘기 직전에 헤더에 동적으로 추가합니다.
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, token);
          }

          return execution.execute(request, body);
        })//
        .build();
  }
}
