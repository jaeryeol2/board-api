package com.board.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.board.api.common.base.ResponseApi;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.request.Signup;
import com.board.api.dtos.response.Home;
import com.board.api.dtos.response.Menu;
import com.board.api.services.board.BoardService;
import com.board.api.services.common.CommonService;
import com.board.api.services.user.UserService;
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
  private final BoardService boardService;


  @GetMapping("/auth/check")
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.authCheck(request, response);
  }

  @PostMapping("/auth/access")
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.getNewAccessToken(request, response);
  }

  @PostMapping("/user/login")
  public CompletableFuture<ResponseApi<Boolean>> login(@RequestBody Login login,
      HttpServletRequest request, HttpServletResponse response) {
    String guestId = request.getHeader("X-Guest-Id");
    return userService.login(guestId, login, request, response);
  }

  @PostMapping({"/user/logout", "/user/token-expire"})
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.logout(request, response);
  }

  @PostMapping("/user/signup")
  public CompletableFuture<ResponseApi<Boolean>> signup(@RequestBody Signup join) {
    return userService.signup(join);
  }

  @GetMapping("/menus")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return commonService.getMenuList();
  }

  @GetMapping("/menus/{boardType}")
  public CompletableFuture<ResponseApi<Menu>> getMenu(@PathVariable String boardType) {
    return commonService.getMenuInfo(BoardType.findByName(boardType).getCode());
  }

  @GetMapping("/home")
  public CompletableFuture<ResponseApi<List<Home>>> getHomeBoardData() {
    return boardService.getHomeBoardData();
  }

}
