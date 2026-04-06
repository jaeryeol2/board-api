package com.project.api.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.dtos.response.User;
import com.project.api.services.read.ReadUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final ReadUserService readUserService;

  @GetMapping("")
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    return readUserService.getUserInfo(request);
  }

}
