package com.board.api.services.user;

import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import com.board.api.common.base.BusinessException;
import com.board.api.common.condition.Case;
import com.board.api.constants.UserRole;
import com.board.api.dtos.request.Signup;
import com.board.api.entities.AppUser;
import com.board.api.repositories.write.WriteUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UserWriter {
  private final PasswordEncoder passwordEncoder;
  private final WriteUserRepository writeUserRepository;

  public boolean signup(Signup join) {
    try {
      final AppUser appUser = writeUserRepository.save(AppUser.builder()//
          .userId(join.getUserId())//
          .password(passwordEncoder.encode(join.getPassword()))//
          .userName(join.getUserName())//
          .email(join.getEmail())//
          .userRole(UserRole.USER_GENERAL.getCode())//
          .build());

      return Optional.of(!ObjectUtils.isEmpty(appUser))//
          .filter(bool -> bool)//
          .map(bool -> bool)//
          .orElse(false);
    } catch (Exception e) {
      log.error("join error : ", e);
      final int firstIndex = 0;
      final String message = e.getMessage();
      final boolean isDuplicate = e instanceof DataIntegrityViolationException;
      final String[] resMessage = new String[] {"회원가입에 실패 했습니다."};

      Case.when(isDuplicate && message.contains("user_id"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 아이디입니다.")//
          .when(isDuplicate && message.contains("email"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 이메일입니다.")//
          .when(isDuplicate)//
          .then(() -> resMessage[firstIndex] = "중복되는 데이터가 있습니다.")//
          .execute();

      throw new BusinessException(resMessage[firstIndex]);
    }

  }

  public boolean updateLastLoginDate(String userId) {
    return writeUserRepository.updateLastLoginDate(userId) > 0;
  }

  @CacheEvict(cacheNames = "getTraficInfo", key = "#id")
  public void removeTraficInfoCache(String id) {
    // empty
  }

  public boolean removeTraficUser(String id) {
    try {
      this.removeTraficInfoCache(id);
      return true;
    } catch (Exception e) {
      log.error("removeTraficUser : {}", e.getMessage());
      return false;
    }
  }
}
