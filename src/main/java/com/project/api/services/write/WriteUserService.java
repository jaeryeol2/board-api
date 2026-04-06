package com.project.api.services.write;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import com.project.api.common.base.BusinessException;
import com.project.api.common.base.ResponseApi;
import com.project.api.common.condition.Case;
import com.project.api.constants.UserRole;
import com.project.api.dtos.request.Join;
import com.project.api.entities.AppUser;
import com.project.api.repositories.write.WriteUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WriteUserService {
  private final WriteUserRepository writeUserRepository;

  @Transactional
  public ResponseApi<Boolean> join(Join join) {
    try {
      final AppUser appUser = writeUserRepository.save(AppUser.builder()//
          .userId(join.getUserId())//
          .password(join.getPassword())//
          .userName(join.getUserName())//
          .email(join.getEmail())//
          .userRole(UserRole.USER_GENERAL.getCode())//
          .build());

      return Optional.of(!ObjectUtils.isEmpty(appUser))//
          .filter(bool -> bool)//
          .map(bool -> ResponseApi.ok(HttpStatus.CREATED, "success", bool))//
          .orElse(ResponseApi.fail("fail", false));
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

  @Transactional
  public ResponseApi<Boolean> updateLastLoginDate(String userId) {
    return ResponseApi.ok(writeUserRepository.updateLastLoginDate(userId) > 0);
  }
}
