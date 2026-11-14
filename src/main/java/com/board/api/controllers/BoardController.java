package com.board.api.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.request.Write.BoardWrite;
import com.board.api.dtos.request.Write.CommentWrite;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.services.board.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
      @PathVariable BoardType boardType, @PathVariable long id, HttpServletRequest request) {
    return boardService.getBoardDetail(boardType, id, request);
  }

  @PostMapping("/{boardType}")
  public ResponseApi<Boolean> writeBoard(@PathVariable BoardType boardType, BoardWrite write,
      HttpServletRequest request) {
    return boardService.writeBoard(boardType, write, request);
  }

  @PutMapping("/{boardType}/{id}")
  public ResponseApi<Boolean> modifyBoard(@PathVariable BoardType boardType, @PathVariable long id,
      BoardWrite write, HttpServletRequest request) {
    return boardService.modifyBoard(boardType, id, write, request);
  }

  @GetMapping("/comment/{boardId}")
  public CompletableFuture<ResponseApi<List<BoardComment>>> getCommentList(
      @PathVariable long boardId) {
    return boardService.getCommentList(boardId);
  }

  @PostMapping("/comment/{boardId}")
  public CompletableFuture<ResponseApi<Boolean>> writeComment(@PathVariable long boardId,
      @RequestBody @Valid CommentWrite write, HttpServletRequest request) {
    return boardService.writeComment(boardId, write, request);
  }

  @PostMapping("/image")
  public CompletableFuture<ResponseApi<String>> uploadImage(
      @RequestParam("file") MultipartFile file, HttpServletRequest request) {
    return boardService.uploadImage(file, request);
  }

  @DeleteMapping("/image")
  public CompletableFuture<ResponseApi<Boolean>> deleteImage(@RequestParam("url") String url,
      HttpServletRequest request) {
    return boardService.deleteImage(url, request);
  }

}
