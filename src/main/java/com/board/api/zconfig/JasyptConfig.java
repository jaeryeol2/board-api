package com.board.api.zconfig;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableEncryptableProperties
public class JasyptConfig {
  @Value("${jasypt.encryptor.password}")
  private String jasyptKey;

  @Bean
  public StringEncryptor encryptor() {
    final PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    final SimpleStringPBEConfig config = new SimpleStringPBEConfig();

    // 암호화키
    config.setPassword(jasyptKey);
    // 알고리즘
    config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
    // 반복할 해싱 회수
    config.setKeyObtentionIterations("1000");
    // 인스턴스 pool
    config.setPoolSize("1");
    config.setProviderName("SunJCE");
    // salt 생성 클래스
    config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
    config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
    // 인코딩 방식
    config.setStringOutputType("base64");
    encryptor.setConfig(config);

    return encryptor;
  }

}
