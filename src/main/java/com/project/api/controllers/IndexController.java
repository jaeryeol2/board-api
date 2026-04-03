package com.project.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.request.Join;
import com.project.api.dtos.request.Login;
import com.project.api.dtos.response.Menu;
import com.project.api.services.CommonService;
import com.project.api.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {
  private final CommonService commonService;
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
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.getNewAccessToken(request, response);
  }


  @PostMapping("/login")
  public CompletableFuture<ResponseApi<Boolean>> login(@RequestBody Login login,
      HttpServletRequest request, HttpServletResponse response) {
    return userService.login(login, request, response);
  }

  @PostMapping("/logout")
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletResponse response) {
    return userService.logout(response);
  }

  @PostMapping("/signup")
  public ResponseApi<Boolean> signup(@RequestBody Join join) {
    return userService.join(join);
  }


  @GetMapping("/menus")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return commonService.getMenuList();
  }

  @GetMapping("/menus/{boardType}")
  public CompletableFuture<ResponseApi<Menu>> getMethodName(@PathVariable String boardType) {
    return commonService.getMenuInfo(BoardType.find(boardType).getCode());
  }



}
