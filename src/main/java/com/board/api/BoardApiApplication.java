package com.board.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class BoardApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(BoardApiApplication.class, args);
  }

}
