package com.project.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.Board;
import com.project.api.dtos.Board.BoardDetail;
import com.project.api.dtos.BoardFilter;
import com.project.api.dtos.BoardWrite;
import com.project.api.services.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
  private final BoardService boardService;

  @GetMapping("/{boardType}")
  public CompletableFuture<ResponseApi<List<Board>>> getBoardList(@PathVariable BoardType boardType,
      BoardFilter boardFilter) {
    log.info("board type name : {}, code : {}", boardType.name(), boardType.getCode());
    return boardService.getBoardList(boardType, boardFilter);
  }

  @GetMapping("/{boardType}/{id}")
  public CompletableFuture<ResponseApi<BoardDetail>> getBoardDetail(
      @PathVariable BoardType boardType, @PathVariable long id) {
    return boardService.getBoardDetail(id);
  }

  @PostMapping("/{boardType}")
  public ResponseApi<Boolean> writeBoard(@PathVariable BoardType boardType, BoardWrite write,
      HttpServletRequest request) {

    return boardService.writeBoard(boardType, write, request);
  }



}
