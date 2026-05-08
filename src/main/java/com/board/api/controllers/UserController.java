package com.board.api.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.board.api.common.base.ResponseApi;
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import com.board.api.services.common.CommonService;
import com.board.api.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final CommonService commonService;
  private final UserService readUserService;

  @GetMapping("")
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    return readUserService.getUserInfo(request);
  }

  @GetMapping("/personal")
  public CompletableFuture<ResponseApi<PersonalUser>> getPersonalUserInfo(
      HttpServletRequest request) {
    return readUserService.getPersonalUserInfo(request);
  }

  @GetMapping("/user-count")
  public CompletableFuture<ResponseApi<Long>> getUserCount() {
    return commonService.getUserCount();
  }



}
