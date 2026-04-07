package com.project.api.zconfig.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class InternalConfig {

  @Bean
  public RestClient internal(RestClient.Builder builder) {
    return builder.baseUrl("")//
        .build();
  }
}
