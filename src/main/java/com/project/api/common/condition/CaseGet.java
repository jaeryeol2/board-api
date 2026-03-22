package com.project.api.common.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CaseGet {
  private CaseGet() {}

  // --- s : initial when ---
  public static InitialWhenBuilder when(BooleanSupplier condition) {
    return new InitialWhenBuilder(condition);
  }

  public static InitialWhenBuilder when(boolean condition) {
    return new InitialWhenBuilder(() -> condition);
  }
  // --- e : initial when ---

  public static class InitialWhenBuilder {
    private final BooleanSupplier currentCondition;

    private InitialWhenBuilder(BooleanSupplier currentCondition) {
      this.currentCondition = currentCondition;
    }

    // 반환(Value) 전용 체인으로 진입 (Supplier<T>)
    public <T> ValueThenBuilder<T> then(Supplier<T> action) {
      List<ValueRule<T>> rules = new ArrayList<>();
      rules.add(new ValueRule<>(this.currentCondition, action));
      return new ValueThenBuilder<>(rules);
    }

    // 값을 직접 넣을 때
    public <T> ValueThenBuilder<T> then(T value) {
      return then(() -> value);
    }
  }

  private static class ValueRule<T> {
    private final BooleanSupplier condition;
    private final Supplier<T> action;

    private ValueRule(BooleanSupplier condition, Supplier<T> action) {
      this.condition = condition;
      this.action = action;
    }
  }

  public static class ValueThenBuilder<T> {
    private final List<ValueRule<T>> rules;

    private ValueThenBuilder(List<ValueRule<T>> rules) {
      this.rules = rules;
    }

    public ValueWhenBuilder<T> when(BooleanSupplier condition) {
      return new ValueWhenBuilder<>(this.rules, condition);
    }

    public ValueWhenBuilder<T> when(boolean condition) {
      return new ValueWhenBuilder<>(this.rules, () -> condition);
    }

    public ValueOtherBuilder<T> other(Supplier<T> action) {
      this.rules.add(new ValueRule<>(() -> true, action));
      return new ValueOtherBuilder<>(this.rules);
    }

    public ValueOtherBuilder<T> other(T value) {
      return other(() -> value);
    }

    public T get() {
      for (ValueRule<T> rule : rules) {
        if (rule.condition.getAsBoolean()) {
          return rule.action.get();
        }
      }

      return null;
    }
  }

  public static class ValueWhenBuilder<T> {
    private final List<ValueRule<T>> rules;
    private final BooleanSupplier currentCondition;

    private ValueWhenBuilder(List<ValueRule<T>> rules, BooleanSupplier currentCondition) {
      this.rules = rules;
      this.currentCondition = currentCondition;
    }

    public ValueThenBuilder<T> then(Supplier<T> action) {
      this.rules.add(new ValueRule<>(this.currentCondition, action));
      return new ValueThenBuilder<>(this.rules);
    }

    public ValueThenBuilder<T> then(T value) {
      return then(() -> value);
    }
  }

  public static class ValueOtherBuilder<T> {
    private final List<ValueRule<T>> rules;

    private ValueOtherBuilder(List<ValueRule<T>> rules) {
      this.rules = rules;
    }

    public T get() {
      return new ValueThenBuilder<>(this.rules).get();
    }
  }
}
