package com.board.api.zconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    // 1. Builder를 통해 스프링 부트의 기본 설정을 유지하면서 ObjectMapper 생성
    // (JavaTimeModule, Jdk8Module 등이 자동 등록됨)
    final ObjectMapper objectMapper = builder.createXmlMapper(false).build();
    // 커스텀 필터 등록
    final SimpleFilterProvider filterProvider =
        new SimpleFilterProvider().addFilter("maskFilter", SimpleBeanPropertyFilter.serializeAll());

    objectMapper.setFilterProvider(filterProvider);

    // 2. 직렬화(Serialization) 설정
    // 날짜 ISO-8601
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // 필드 없는 객체 에러 무시
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    // 빈 문자열, 빈 컬렉션도 뺄 거라면 NON_EMPTY 권장
    objectMapper.setSerializationInclusion(Include.NON_NULL);

    // 3. 역직렬화(Deserialization) 설정
    // 모르는 필드 무시
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // 모르는 Enum은 null 처리
    objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    return objectMapper;
  }
}
