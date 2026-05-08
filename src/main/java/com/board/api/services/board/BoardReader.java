package com.board.api.services.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.constants.BoardType;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.Board.BoardDetail;
import com.board.api.dtos.response.Home;
import com.board.api.dtos.response.Menu;
import com.board.api.repositories.read.ReadBoardRepository;
import com.board.api.services.common.CommonReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardReader {
  private final CommonReader commonReader;
  private final ReadBoardRepository readBoardRepository;

  @Cacheable(cacheNames = "getHomeBoardData", unless = "#result == null || #result.size() <= 0")
  public List<Home> getHomeBoardData() {
    final List<Home> homes = new ArrayList<>();
    List<Menu> menuList = Optional.ofNullable(commonReader.getMenuList()).orElse(new ArrayList<>());
    List<String> boardTypeCodeList = menuList.stream().map(Menu::getBoardCode).toList();
    Map<String, Menu> menuMap =
        menuList.stream().collect(Collectors.toMap(Menu::getBoardCode, Function.identity()));


    for (String boardTypeCode : boardTypeCodeList) {
      final Home home = new Home();
      final List<Board> boards = Optional
          .ofNullable(readBoardRepository.findPopularBoardListByBoardTypeCode(boardTypeCode))//
          .orElse(new ArrayList<>());
      final Optional<Menu> menu = Optional.ofNullable(menuMap.get(boardTypeCode));

      if (menu.isPresent() && !boards.isEmpty()) {
        home.setMenuName(menu.get().getMenuName());
        home.setBasePath(BoardType.findByCode(boardTypeCode));
        home.setHref(menu.get().getHref());
        home.setBoards(boards);

        homes.add(home);
      }
    }

    return homes;
  }

  @Cacheable(cacheNames = "getBoardList", key = "#type.code + '_' + #filter.page")
  public ResponseList<Board> getBoardList(final BoardType type, final BoardFilter filter) {
    final String boardTypeCode = type.getCode();
    filter.setBoardTypeCode(boardTypeCode);
    final long count = readBoardRepository.findBoardCountById(filter);

    return ResponseList.of(Math.toIntExact(count), CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardByIds(filter);
          return readBoardRepository.findBoardList(boardTypeCode, ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }

  @Cacheable(cacheNames = "getBoardList", key = "'board-popular' + '_' + #filter.page")
  public ResponseList<Board> getBoardPopularList(final BoardFilter filter) {
    final long count = readBoardRepository.findBoardPopularCount();

    return ResponseList.of(Math.toIntExact(count), CaseGet.when(count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardPopularByIds(filter);
          return readBoardRepository.findBoardPopularList(ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }


  @Cacheable(cacheNames = "getBoardDetail", key = "'board-id-' + #id")
  public BoardDetail getBoardDetail(final Long id) {
    Optional<BoardDetail> optional = Optional.ofNullable(readBoardRepository.findBoardById(id));
    BoardDetail detail = null;

    if (optional.isPresent()) {
      detail = optional.get();

      detail.setCommentList(readBoardRepository.findBoardCommentByBoardId(id));
      detail.setFileList(readBoardRepository.findBoardFileByBoardId(id));

    } else {
      throw new BusinessException("존재하지 않는 게시물입니다.");
    }

    return detail;
  }

  public List<BoardComment> getCommentList(long boardId) {
    return readBoardRepository.findBoardCommentByBoardId(boardId);
  }
}
