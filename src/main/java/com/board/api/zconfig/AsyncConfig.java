package com.board.api.zconfig;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

  private static final String VIRTUAL_THREAD_NAME = "app-virtual-thread-";

  @Bean
  @Primary
  public TaskExecutor appAsync() {
    // 1. 가상 스레드 기반 Executor 생성
    final TaskExecutorAdapter executor =
        new TaskExecutorAdapter(new VirtualThreadTaskExecutor(VIRTUAL_THREAD_NAME));

    // 2. TaskDecorator 설정 (MDC 복사)
    executor.setTaskDecorator(runnable -> {

      // [실행 시점: 메인 톰캣 스레드]
      // 비동기 호출을 지시한 톰캣 스레드에서 기존 로깅 컨텍스트(TraceId 등)를 캡처합니다.
      log.info("Preparing async task on caller thread: {}", Thread.currentThread());
      final Map<String, String> contextMap = MDC.getCopyOfContextMap();
      final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

      return () -> {
        // [실행 시점: 가상 스레드]
        log.info("Executing async task on virtual thread: {}", Thread.currentThread());
        try {
          // 메인 스레드의 MDC를 가상 스레드에 주입
          if (contextMap != null) {
            MDC.setContextMap(contextMap);
          }
          // 메인 스레드의 RequestContext를 가상 스레드에 주입
          if (requestAttributes != null) {
            RequestContextHolder.setRequestAttributes(requestAttributes);
          }

          runnable.run(); // 실제 @Async 비즈니스 로직 실행
        } finally {
          // 스레드 종료 시점 정리 (메모리 릭 방지)
          MDC.clear();
          RequestContextHolder.resetRequestAttributes();
        }
      };
    });

    // 3. Spring Security 컨텍스트 위임 (TaskExecutor 타입에 맞게 AsyncTaskExecutor 래퍼 사용)
    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }

  // s : spring.threads.virtual.enable=true(클라이언트 요청부터 가상스레드로 받음) MDC설정 방법

  // // 1. MDC를 복사 전파 동기화할 데코레이터 정의
  // // @Bean
  // public TaskDecorator mdcTaskDecorator() {
  // return runnable -> {
  // Map<String, String> contextMap = MDC.getCopyOfContextMap();

  // return () -> {
  // try {
  // if (Objects.nonNull(contextMap)) {
  // MDC.setContextMap(contextMap);
  // }

  // runnable.run();
  // } finally {
  // MDC.clear();
  // }
  // };
  // };
  // }

  // // 2. 스프링 부트의 가상스레드 익스큐터 빌더에 데코레이터 주입
  // // @Async 가상 스레드 구동 시 MDC가 전파됩니다.
  // @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
  // public AsyncTaskExecutor appTaskExecutor(SimpleAsyncTaskExecutorBuilder builder,
  // @Qualifier("mdcTaskDecorator") TaskDecorator taskDecorator) {
  // return builder.taskDecorator(taskDecorator).build();
  // }

  // e : spring.threads.virtual.enable=true(클라이언트 요청부터 가상스레드로 받음) MDC설정 방법
}
