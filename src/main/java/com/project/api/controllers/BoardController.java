package com.project.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.constants.BoardType;
import com.project.api.dtos.BoardFilter;
import com.project.api.services.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
  private final BoardService boardService;

  @GetMapping("/{boardType}")
  public CompletableFuture<ResponseApi<List<?>>> getBoardList(@PathVariable BoardType boardType,
      BoardFilter boardFilter) {
    return boardService.getBoardList(boardType, boardFilter);
  }

}
