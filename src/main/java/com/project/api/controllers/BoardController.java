package com.project.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.base.ResponseApi.ResponseList;
import com.project.api.common.condition.CaseGet;
import com.project.api.constants.BoardType;
import com.project.api.dtos.request.BoardFilter;
import com.project.api.dtos.request.BoardWrite;
import com.project.api.dtos.request.CommentWrite;
import com.project.api.dtos.response.Board;
import com.project.api.dtos.response.Board.BoardComment;
import com.project.api.dtos.response.Board.BoardDetail;
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
  public CompletableFuture<ResponseApi<ResponseList<Board>>> getBoardList(
      @PathVariable BoardType boardType, BoardFilter boardFilter) {
    log.info("board type name : {}, code : {}", boardType.name(), boardType.getCode());
    log.info("board filter parameter : {}", boardFilter);

    return CaseGet.when(() -> BoardType.BOARD_POPULAR == boardType)//
        .then(() -> boardService.getBoardPopularList(boardFilter))//
        .other(() -> boardService.getBoardList(boardType, boardFilter))//
        .get();

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

  @GetMapping("/comment/{boardId}")
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(
      @PathVariable long boardId) {
    return boardService.getCommentList(boardId);
  }

  @PostMapping("/comment/{boardId}")
  public ResponseApi<Boolean> writeComment(@PathVariable long boardId,
      @RequestBody CommentWrite write, HttpServletRequest request) {
    return boardService.writeComment(boardId, write, request);
  }



}
