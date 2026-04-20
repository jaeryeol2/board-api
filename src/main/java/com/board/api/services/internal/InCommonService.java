package com.board.api.services.internal;

import java.util.List;
import org.springframework.stereotype.Service;
import com.board.api.dtos.response.Menu;
import com.board.api.repositories.read.ReadMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InCommonService {
  private final ReadMenuRepository readMenuRepository;

  public List<Menu> getMenuList() {
    return readMenuRepository.findMenuList();
  }
}
