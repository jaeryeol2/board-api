package com.project.api.zconfig.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentConfig {

  @Bean
  public RestClient naver(RestClient.Builder builder) {
    return builder.baseUrl("https://pay.naver.com")//
        .build();
  }

  @Bean
  public RestClient kakao(RestClient.Builder builder) {
    return builder.baseUrl("https://kapi.kakao.com")//
        .build();
  }

  @Bean
  public RestClient toss(RestClient.Builder builder) {
    return builder.baseUrl("https://api.tosspayments.com")//
        .build();
  }

}
