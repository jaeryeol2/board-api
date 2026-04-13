package com.board.api.zconfig;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.board.api.constants.BoardType;
import com.board.api.zconfig.entrypoint.AuthEntryPoint;
import com.board.api.zconfig.filters.AuthFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  @Value("${spring.profiles.active}")
  private String active;
  @Value("${front.url}")
  private String frontURL;
  private final AuthFilter authFilter;
  private final AuthEntryPoint authEntryPoint;
  private static final String[] IGNORE_PATH_ARRAY =
      {"/actuator/health", "/login", "/logout", "/token-expire", "/signup", "/menus/**"};

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.httpBasic(basic -> basic.disable()).csrf(csrf -> csrf.disable())
        .formLogin(login -> login.disable()).logout(logout -> logout.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling -> handling.authenticationEntryPoint(authEntryPoint))
        .cors(cors -> cors.configurationSource(configurationSource()))
        // Spring MVC의 비동기 처리(@Async 컨트롤러)에서 SecurityContext를 유지하기 위한 설정.
        // 요청(Request) 객체 자체에 SecurityContext를 저장하여, 요청의 전체 생명주기 동안 인증 정보를 보존.
        .securityContext(context -> context
            .securityContextRepository(new RequestAttributeSecurityContextRepository()))
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(IGNORE_PATH_ARRAY).permitAll();
          auth.requestMatchers(HttpMethod.GET, "/board/{boardType}",
              "/board/".concat(BoardType.BOARD_FREE.name()).concat("/{id}"))//
              .permitAll();

          if (!"prod".equalsIgnoreCase(active)) {
            auth.requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
                .permitAll();
          } else {
            auth.requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
                .denyAll();
          }

          auth.anyRequest().authenticated();
        });

    return http.build();
  }


  @Bean
  CorsConfigurationSource configurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    config.setAllowedOrigins(Arrays.asList(frontURL.split(",")));
    config.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION));
    config.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name()));

    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
