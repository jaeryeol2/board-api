package com.board.api.services.user;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import com.board.api.dtos.request.UserProfileUpdate;
import com.board.api.entities.AppUser;
import com.board.api.repositories.write.WriteUserRepository;
import com.board.api.services.common.CommonReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UserWriter {
  private final PasswordEncoder passwordEncoder;
  private final WriteUserRepository writeUserRepository;
  private final CommonReader commonReader;

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

      Case.when(() -> isDuplicate && message.contains("user_id"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 아이디입니다.")//
          .when(() -> isDuplicate && message.contains("email"))//
          .then(() -> resMessage[firstIndex] = "이미 존재하는 이메일입니다.")//
          .when(() -> isDuplicate)//
          .then(() -> resMessage[firstIndex] = "중복되는 데이터가 있습니다.")//
          .execute();

      throw new BusinessException(resMessage[firstIndex]);
    }

  }

  public boolean updateLastLoginDate(String userId) {
    return writeUserRepository.findByUserId(userId)//
        .map(user -> {
          user.updateLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
          return true;
        })//
        .orElse(false);
  }

  @CacheEvict(cacheNames = "getUserInfo", key = "#userId")
  public boolean updateProfile(String userId, UserProfileUpdate updateDto) {
    return writeUserRepository.findByUserId(userId)//
        .map(user -> {
          user.updateProfile(updateDto.getUserName(), updateDto.getEmail(), updateDto.getUserAddr(),
              updateDto.getUserGender());
          return true;
        })//
        .orElse(false);
  }

  public boolean updatePassword(String userId, String currentPassword, String newPassword) {
    return writeUserRepository.findByUserId(userId)//
        .map(user -> {
          if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("현재 비밀번호가 일치하지 않습니다.");
          }
          user.updatePassword(passwordEncoder.encode(newPassword));
          return true;
        })//
        .orElse(false);
  }

  public void removeTrafficInfoCache(String id) {
    commonReader.removeUserActivity(id);
  }

}
