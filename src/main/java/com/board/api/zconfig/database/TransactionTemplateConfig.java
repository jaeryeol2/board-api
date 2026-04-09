package com.board.api.zconfig.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionTemplateConfig {

  @Bean
  public TransactionTemplate writeTransaction(PlatformTransactionManager transactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    // 핵심 설정: 기존 트랜잭션을 무시하고 새 트랜잭션을 생성 (Write DB 분기 유도)
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    // 쓰기 가능 모드 명시
    transactionTemplate.setReadOnly(false);
    // (선택) 타임아웃 등 추가 설정 가능
    transactionTemplate.setTimeout(30);

    return transactionTemplate;
  }

  @Bean
  public TransactionTemplate readTransaction(PlatformTransactionManager transactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    transactionTemplate.setReadOnly(true);
    transactionTemplate.setTimeout(30);

    return transactionTemplate;
  }
}
