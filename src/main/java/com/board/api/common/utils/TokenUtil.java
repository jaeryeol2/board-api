package com.board.api.common.utils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class TokenUtil {
  @Value("${jwt.secret.key}")
  private String secretKeyString;

  private SecretKey secretKey;

  @PostConstruct
  protected void init() {
    // secret key 문자열을 UTF-8 바이트 배열로 변환하고, 이를 기반으로 SecretKey 객체를 생성합니다.
    // 이 키는 HS256 알고리즘에 사용됩니다.
    this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(final String username, final long hour) {
    final Instant now = Instant.now();
    return Jwts.builder().subject(username).issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(Duration.ofHours(hour))))
        // HS256 알고리즘으로 서명
        .signWith(secretKey, Jwts.SIG.HS256).compact();
  }

  public Claims getTokenInfo(final String token) {
    try {
      Jws<Claims> jws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return jws.getPayload();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  public boolean isTokenExpired(final String token) {
    try {
      Jws<Claims> jws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return jws.getPayload().getExpiration().toInstant().isBefore(Instant.now());
    } catch (Exception e) {
      return true;
    }
  }


}
