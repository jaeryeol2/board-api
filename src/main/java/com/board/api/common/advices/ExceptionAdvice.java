package com.board.api.common.advices;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;

@RestControllerAdvice
public class ExceptionAdvice {

  @ExceptionHandler(BusinessException.class)
  public ResponseApi<Object> exceptionHandler(final BusinessException e) {
    return ResponseApi.fail(e.getStatus(), e.getCode(), e.getMessage());
  }
}
