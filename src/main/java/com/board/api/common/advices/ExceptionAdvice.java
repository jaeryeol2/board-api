package com.board.api.common.advices;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.board.api.common.base.BusinessException;
import com.board.api.common.base.ResponseApi;
import lombok.extern.slf4j.Slf4j;

/**
 * 공통 예외 처리.
 * 응답 본문(ResponseApi)의 status·code·message 규격은 유지하고, HTTP 상태 코드만 실제 오류 상태로 내려줍니다.
 * (HTTP 200 오류 응답이 Next 캐시에 저장되는 문제 방지)
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ResponseApi<Object>> runtimeExceptionHandler(final BusinessException e) {
    log.error("BusinessException : ", e);

    return ResponseEntity.status(toHttpStatus(e.getStatus()))
        .body(ResponseApi.fail(e.getStatus(), e.getCode(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseApi<Object>> validationExceptionHandler(
      final MethodArgumentNotValidException e) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final String message = Optional.ofNullable(e.getBindingResult().getFieldError())
        .map(FieldError::getDefaultMessage).orElse("입력값이 올바르지 않습니다.");
    log.warn("validation error : {}", message);

    return ResponseEntity.status(status)
        .body(ResponseApi.fail(status.value(), status.name(), message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseApi<Object>> exceptionHandler(final Exception e) {
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    log.error("Exception : ", e);

    // 내부 예외 메시지(SQL·제약조건명 등)는 로그에만 남기고 고정 메시지를 반환합니다.
    return ResponseEntity.status(status)
        .body(ResponseApi.fail(status.value(), status.name(), "요청을 처리하는 중 오류가 발생했습니다."));
  }

  /**
   * 비즈니스 오류 코드(기본 999 등)가 4xx/5xx가 아니면 400으로 응답합니다.
   */
  private static HttpStatus toHttpStatus(final int status) {
    final HttpStatus httpStatus = HttpStatus.resolve(status);
    return httpStatus != null && httpStatus.isError() ? httpStatus : HttpStatus.BAD_REQUEST;
  }
}
