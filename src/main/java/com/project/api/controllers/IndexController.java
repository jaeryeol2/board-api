package com.project.api.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.dtos.Login;
import com.project.api.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {

  private final UserService userService;

  @GetMapping("/status")
  public ResponseApi<String> status() {
    return ResponseApi.ok("OK");
  }

  @GetMapping("/auth")
  public CompletableFuture<ResponseApi<Boolean>> auth(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.authCheck(request, response);
  }

  @PostMapping("/auth/access")
  public CompletableFuture<ResponseApi<Boolean>> getToken(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.getAccessToken(request, response);
  }


  @PostMapping("/login")
  public CompletableFuture<ResponseApi<Boolean>> login(@RequestBody Login login,
      HttpServletRequest request, HttpServletResponse response) {
    return userService.login(login, request, response);
  }

  @PostMapping("/logout")
  public ResponseApi<Boolean> postMethodName(@RequestBody String entity) {
    return ResponseApi.ok(true);
  }

}
