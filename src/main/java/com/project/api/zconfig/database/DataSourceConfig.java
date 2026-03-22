package com.project.api.zconfig.database;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {
  private static final String AOP_POINTCUT_EXPRESSION =
      "execution(* com.project.api.services..*Service.*(..))";

  public static class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
      return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "read" : "write";
    }

  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.write.hikari")
  DataSource writeHikariDataSource() {
    return new HikariDataSource();
  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.read.hikari")
  DataSource readHikariDataSource() {
    return new HikariDataSource();
  }

  @Bean
  public DataSource routingDataSource(
      @Qualifier("writeHikariDataSource") DataSource writeHikariDataSource,
      @Qualifier("readHikariDataSource") DataSource readHikariDataSource) {

    RoutingDataSource routingDataSource = new RoutingDataSource();

    Map<Object, Object> dataSourceMap = new HashMap<>();
    dataSourceMap.put("write", writeHikariDataSource);
    dataSourceMap.put("read", readHikariDataSource);

    routingDataSource.setTargetDataSources(dataSourceMap);
    routingDataSource.setDefaultTargetDataSource(writeHikariDataSource);

    return routingDataSource;
  }

  // 5. LazyConnection 프록시 적용 (실제 쿼리 발생 시점에 커넥션 획득)
  @Bean
  @Primary
  public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
    return new LazyConnectionDataSourceProxy(routingDataSource);
  }

  @Bean
  // TransactionManager 파라미터를 제거하여 빈 생성 시점의 강제 의존성을 끊습니다.
  public TransactionInterceptor transactionAdvice() {
    NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();

    RuleBasedTransactionAttribute readOnlyRule = new RuleBasedTransactionAttribute();
    readOnlyRule.setReadOnly(true);

    RuleBasedTransactionAttribute writeRule = new RuleBasedTransactionAttribute();
    writeRule.setReadOnly(false);

    Map<String, org.springframework.transaction.interceptor.TransactionAttribute> txMethods =
        new HashMap<>();
    // 읽기 전용 메서드 패턴
    txMethods.put("get*", readOnlyRule);
    txMethods.put("find*", readOnlyRule);
    txMethods.put("read*", readOnlyRule);
    txMethods.put("search*", readOnlyRule);
    txMethods.put("select*", readOnlyRule);

    // 쓰기/수정/삭제 등 나머지 모든 메서드
    txMethods.put("*", writeRule);

    source.setNameMap(txMethods);

    TransactionInterceptor interceptor = new TransactionInterceptor();
    interceptor.setTransactionAttributeSource(source);
    // 스프링 빈 이름으로 지정해두면, 실제 트랜잭션이 발생하는 런타임 시점에 해당 빈을 찾아 사용합니다.
    interceptor.setTransactionManagerBeanName("transactionManager");

    return interceptor;
  }

  @Bean
  public Advisor transactionAdviceAdvisor(TransactionInterceptor transactionAdvice) {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression(AOP_POINTCUT_EXPRESSION);

    // Advisor 빈만 등록해두면 Spring이 알아서 AOP 프록시를 적용합니다.
    return new DefaultPointcutAdvisor(pointcut, transactionAdvice);
  }


}
