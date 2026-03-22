package com.project.api.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import com.project.api.common.base.BusinessException;

public class Validator {
  private Validator() {}

  private static class ValidatorType {
    private final BooleanSupplier condition;
    private final String message;

    private ValidatorType(BooleanSupplier condition, String message) {
      this.condition = condition;
      this.message = message;
    }

    private boolean validate() {
      if (condition.getAsBoolean()) {
        if (message != null && !message.isBlank()) {
          throw new BusinessException(message);
        }

        return false;
      }

      return true;
    }
  }

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

  public static class ValidatorBuilder {
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

    public boolean validate() {
      return this.validators.stream().allMatch(ValidatorType::validate);
    }
  }
}
