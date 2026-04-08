package com.board.api.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import com.board.api.common.base.BusinessException;

public interface Validator {

  boolean validate();

  public static ValidatorBuilder verification(BooleanSupplier condition, String message) {
    List<ValidatorType> validatorTypes = new ArrayList<>();
    validatorTypes.add(new ValidatorType(condition, message));

    return new ValidatorBuilder(validatorTypes);
  }

  public static ValidatorBuilder verification(boolean condition, String message) {
    return verification(() -> condition, message);
  }

  public static ValidatorBuilder verification(BooleanSupplier condition) {
    return verification(condition, "");
  }

  public static ValidatorBuilder verification(boolean condition) {
    return verification(() -> condition);
  }

  // s : ValidatorBuilder
  class ValidatorBuilder {
    private List<ValidatorType> validators;

    private ValidatorBuilder(List<ValidatorType> list) {
      this.validators = list;
    }

    public ValidatorBuilder verification(BooleanSupplier condition, String message) {
      this.validators.add(new ValidatorType(condition, message));
      return new ValidatorBuilder(this.validators);
    }

    public ValidatorBuilder verification(boolean condition, String message) {
      return verification(() -> condition, message);
    }

    public ValidatorBuilder verification(BooleanSupplier condition) {
      return verification(condition, "");
    }

    public ValidatorBuilder verification(boolean condition) {
      return verification(() -> condition);
    }

    public Validator build() {
      return new DefaultValidator(validators);
    }

    public boolean validate() {
      return this.validators.stream().allMatch(ValidatorType::validate);
    }
  }
  // e : ValidatorBuilder
}


// s : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
class DefaultValidator implements Validator {
  private List<ValidatorType> validators;

  DefaultValidator(List<ValidatorType> validators) {
    this.validators = List.copyOf(validators);
  }

  @Override
  public boolean validate() {
    return this.validators.stream().allMatch(ValidatorType::validate);
  }
}


class ValidatorType {
  private final BooleanSupplier condition;
  private final String message;

  ValidatorType(BooleanSupplier condition, String message) {
    this.condition = condition;
    this.message = message;
  }

  boolean validate() {
    if (condition.getAsBoolean()) {
      if (message != null && !message.isBlank()) {
        throw new BusinessException(message);
      }

      return false;
    }

    return true;
  }
}
// e : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
