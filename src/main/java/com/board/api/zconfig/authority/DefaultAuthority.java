package com.board.api.zconfig.authority;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class DefaultAuthority extends UsernamePasswordAuthenticationToken {

  public DefaultAuthority(Object principal, String credentials) {
    super(principal, credentials);
  }

  public DefaultAuthority(Object principal, String credentials,
      List<GrantedAuthority> authorities) {
    super(principal, credentials, authorities);
  }
}
