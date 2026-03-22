package com.project.api.zconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

  @Bean
  public ObjectMapper objectMapper() {
    final SimpleFilterProvider filterProvider =
        new SimpleFilterProvider().addFilter("maskFilter", SimpleBeanPropertyFilter.serializeAll());

    return new ObjectMapper().setFilterProvider(filterProvider).registerModule(new JavaTimeModule())
        // JSON/Map에 객체에 없는 필드가 있어도 에러를 발생시키지 않음
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
