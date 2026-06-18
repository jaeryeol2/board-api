package com.board.api.common.advices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

  @ExceptionHandler(BusinessException.class)
  public ResponseApi<Object> runtimeExceptionHandler(final BusinessException e) {
    log.error("BusinessException : ", e);

    return ResponseApi.fail(e.getStatus(), e.getCode(), e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseApi<Object> exceptionHandler(final Exception e) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    log.error("Exception : ", e);

    return ResponseApi.fail(status.value(), status.name(), e.getMessage());
  }
}
