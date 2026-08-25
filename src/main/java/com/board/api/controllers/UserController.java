package com.board.api.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.board.api.common.base.ResponseApi;
import com.board.api.dtos.request.PasswordVerify;
import com.board.api.dtos.request.UserPasswordUpdate;
import com.board.api.dtos.request.UserProfileUpdate;
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import com.board.api.services.common.CommonService;
import com.board.api.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final CommonService commonService;
  private final UserService userService;

  @GetMapping("")
  public CompletableFuture<ResponseApi<User>> getUserInfo(HttpServletRequest request) {
    return userService.getUserInfo(request);
  }

  @GetMapping("/personal")
  public CompletableFuture<ResponseApi<PersonalUser>> getPersonalUserInfo(
      HttpServletRequest request) {
    return userService.getPersonalUserInfo(request);
  }

  @PostMapping("/verify-password")
  public CompletableFuture<ResponseApi<Boolean>> verifyPassword(HttpServletRequest request,
      @RequestBody @Valid PasswordVerify verifyDto) {
    return userService.verifyPassword(request, verifyDto);
  }

  @PutMapping("/profile")
  public CompletableFuture<ResponseApi<Boolean>> updateProfile(HttpServletRequest request,
      @RequestBody @Valid UserProfileUpdate updateDto) {
    return userService.updateProfile(request, updateDto);
  }

  @PutMapping("/password")
  public CompletableFuture<ResponseApi<Boolean>> updatePassword(HttpServletRequest request,
      HttpServletResponse response, @RequestBody @Valid UserPasswordUpdate updateDto) {
    return userService.updatePassword(request, response, updateDto);
  }

  @GetMapping("/user-count")
  public CompletableFuture<ResponseApi<Long>> getUserCount() {
    return commonService.getUserCount();
  }

}
