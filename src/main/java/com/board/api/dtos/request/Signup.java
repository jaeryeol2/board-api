package com.board.api.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@ToString(exclude = "password")
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signup {
  @NotBlank(message = "아이디를 입력해주세요.")
  @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "아이디는 영문, 숫자, _ 조합 4~20자로 입력해주세요.")
  private String userId;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해주세요.")
  private String password;

  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
  private String email;

  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
  private String userName;
}
