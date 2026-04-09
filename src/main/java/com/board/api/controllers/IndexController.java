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
import com.board.api.dtos.request.Join;
import com.board.api.dtos.request.Login;
import com.board.api.dtos.response.Menu;
import com.board.api.services.internal.InUserService;
import com.board.api.services.read.CommonService;
import com.board.api.services.read.ReadBoardService;
import com.board.api.services.read.ReadUserService;
import com.board.api.services.write.WriteUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {
  private final InUserService inUserService;
  private final CommonService commonService;
  private final ReadUserService readUserService;
  private final ReadBoardService readBoardService;
  private final WriteUserService writeUserService;


  @GetMapping("/auth/check")
  public CompletableFuture<ResponseApi<Boolean>> authCheck(HttpServletRequest request) {
    return ResponseApi.completedOk(inUserService.checkToken(request));
  }

  @PostMapping("/auth/access")
  public CompletableFuture<ResponseApi<Boolean>> getNewAccessToken(HttpServletRequest request,
      HttpServletResponse response) {
    return readUserService.getNewAccessToken(request, response);
  }

  @PostMapping("/login")
  public CompletableFuture<ResponseApi<Boolean>> login(@RequestBody Login login,
      HttpServletRequest request, HttpServletResponse response) {
    return readUserService.login(login, request, response);
  }

  @PostMapping({"/logout", "/token-expire"})
  public CompletableFuture<ResponseApi<Boolean>> logout(HttpServletRequest request,
      HttpServletResponse response) {
    String userId = inUserService.getUserId(request);
    return writeUserService.logout(userId, response);
  }

  @PostMapping("/signup")
  public ResponseApi<Boolean> signup(@RequestBody Join join) {
    return writeUserService.join(join);
  }


  @GetMapping("/menus")
  public CompletableFuture<ResponseApi<List<Menu>>> getMenuList() {
    return commonService.getMenuList();
  }

  @GetMapping("/menus/{boardType}")
  public CompletableFuture<ResponseApi<Menu>> getMethodName(@PathVariable String boardType) {
    return commonService.getMenuInfo(BoardType.find(boardType).getCode());
  }

  @GetMapping("/home")
  public CompletableFuture<ResponseApi<List<?>>> getHomeRenderData() {
    return readBoardService.getHomRenderData();
  }



}
