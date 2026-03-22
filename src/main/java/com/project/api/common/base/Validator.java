package com.project.api.common.base;

import java.util.List;

@FunctionalInterface
public interface Validator {
  boolean validate();

  static Validator invalid(boolean condition, String message) {
    return () -> {
      if (condition) {
        throw new BusinessException(message);
      }

      return true;
    };
  }

  static boolean validate(List<Validator> validators) {
    return validators.stream().allMatch(Validator::validate);
  }
}
