package com.project.api.zconfig;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableFeignClients(defaultConfiguration = FeignConfig.class)
public class FeignConfig {

  /**
   * Feign 요청에 현재 요청의 Authorization 헤더를 전파하는 인터셉터를 등록합니다. 이를 통해 MSA 환경에서 서비스 간 인증 토큰을 원활하게 전달할 수
   * 있습니다.
   */
  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication != null && authentication.getCredentials() != null) {
        // 2. 인증 객체에서 토큰 추출 (Security 설정에 따라 getCredentials() 또는 getDetails() 등에 토큰이 있을 수 있습니다)
        String token = authentication.getCredentials().toString();
        String authorizationHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        log.debug("Propagating Authorization header from SecurityContext to Feign request.");
        requestTemplate.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
      } else {
        log.debug(
            "No authentication found in SecurityContext. Feign request will be sent without Authorization header.");
      }
    };
  }
}
