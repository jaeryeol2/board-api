package com.board.api.zconfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncodeConfig {
  /**
   * password용 단방향 암호화 encoder(복호화는 X)
   * 
   * @return
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    // Argon2PasswordEncoder의 최신 권장 설정 (OWASP 기준)
    // saltLength=16, hashLength=32, parallelism=1, memory=65536 (64MB),
    // iterations=10
    // Spring Security 6.2+ 에서는 아래와 같이 빌더(builder) 패턴을 권장합니다.
    Argon2PasswordEncoder argon2Encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 10);

    // 레거시 또는 다른 인코더가 필요할 경우 Map에 추가
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("argon2", argon2Encoder);
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    // 필요하다면 pbkdf2, scrypt 등 다른 인코더도 추가할 수 있습니다.

    // DelegatingPasswordEncoder를 생성하고, 기본 인코더를 argon2로 지정합니다.
    DelegatingPasswordEncoder delegatingPasswordEncoder =
        new DelegatingPasswordEncoder("argon2", encoders);

    // 생성된 인코더의 기본 인코딩 알고리즘을 argon2로 명시적으로 설정
    delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(argon2Encoder);

    return new ConcurrencyLimitedPasswordEncoder(delegatingPasswordEncoder);
  }

  /**
   * Argon2는 해싱 1회당 약 64MB를 사용하므로, 가상 스레드에서 무제한 동시 실행되면 메모리가 고갈됩니다.
   * 동시 해싱 수를 제한하고 초과 요청은 대기시킵니다.
   */
  private static final class ConcurrencyLimitedPasswordEncoder implements PasswordEncoder {
    private static final int MAX_CONCURRENT_HASHING = 4;

    private final PasswordEncoder delegate;
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_HASHING, true);

    private ConcurrencyLimitedPasswordEncoder(PasswordEncoder delegate) {
      this.delegate = delegate;
    }

    @Override
    public String encode(CharSequence rawPassword) {
      return limit(() -> delegate.encode(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
      return limit(() -> delegate.matches(rawPassword, encodedPassword));
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
      return delegate.upgradeEncoding(encodedPassword);
    }

    private <T> T limit(Supplier<T> task) {
      semaphore.acquireUninterruptibly();
      try {
        return task.get();
      } finally {
        semaphore.release();
      }
    }
  }
}
