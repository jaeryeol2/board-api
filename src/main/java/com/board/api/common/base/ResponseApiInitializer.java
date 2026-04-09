package com.board.api.common.base;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResponseApiInitializer {
  private final TaskExecutor appAsync;

  @PostConstruct
  public void init() {
    ResponseApi.setExecutor(appAsync);
  }
}
