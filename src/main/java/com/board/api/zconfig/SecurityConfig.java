package com.board.api.zconfig;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import com.board.api.constants.ProfileActive;
import com.board.api.zconfig.entrypoint.AuthAccessDeniedHandler;
import com.board.api.zconfig.entrypoint.AuthEntryPoint;
import com.board.api.zconfig.filters.AuthFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  @Value("${spring.profiles.active}")
  private String active;

  private final AuthFilter authFilter;
  private final AuthEntryPoint authEntryPoint;
  private final AuthAccessDeniedHandler authAccessDeniedHandler;

  private static final String[] IGNORE_PATH_ARRAY =
      {"/actuator/health", "/home", "/user/login", "/user/logout", "/auth/token-expire",
          "/auth/guest", "/user/user-count", "/user/signup", "/menus/**"};
  private static final String[] SWAGGER_PATH_ARRAY =
      {"/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**"};

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.httpBasic(basic -> basic.disable()).csrf(csrf -> csrf.disable())
        .formLogin(login -> login.disable()).logout(logout -> logout.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling -> handling.authenticationEntryPoint(authEntryPoint)
            .accessDeniedHandler(authAccessDeniedHandler)) // 403 핸들러 추가
        .cors(cors -> cors.configurationSource(configurationSource()))
        .securityContext(context -> context
            .securityContextRepository(new RequestAttributeSecurityContextRepository()))
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(IGNORE_PATH_ARRAY).permitAll();
          auth.requestMatchers(HttpMethod.GET, "/board/{boardType}",
              "/board/".concat(BoardType.BOARD_FREE.name()).concat("/{id}"))//
              .permitAll();

          if (!ProfileActive.PROD.getValue().equalsIgnoreCase(active)) {
            auth.requestMatchers(SWAGGER_PATH_ARRAY).permitAll();
          } else {
            auth.requestMatchers(SWAGGER_PATH_ARRAY).denyAll();
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
    config.setMaxAge(Duration.ofHours(1L));
    config.setAllowedHeaders(List.of("*"));

    // 보완: 모든 REST Method 허용 (PUT, PATCH, DELETE, OPTIONS 추가)
    config.setAllowedMethods(
        List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));

    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
