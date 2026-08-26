package com.board.api.common.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * 다중 조건에 따라 값을 매핑/반환(Expression)하는 유틸리티입니다.
 * 
 * <p>
 * <b>사용 가이드:</b>
 * </p>
 * <ul>
 * <li><b>지연 평가 (NPE 안전 & 비용 지연):</b>
 * {@code when(() -> user.isAdmin()).then(() -> expensiveCalc())}</li>
 * </ul>
 */
public interface CaseGet<T> {

  // --- 값을 반환하는 실행 메서드 ---
  T get();

  /**
   * 지연 평가 조건식으로 분기를 시작합니다. (NPE 방지 및 단락 평가 보장)
   */
  static InitialWhenBuilder when(BooleanSupplier condition) {
    return new InitialWhenBuilder(condition);
  }

  // --- Builders (인터페이스 내부의 class는 자동으로 public static) ---
  class InitialWhenBuilder {
    private final BooleanSupplier currentCondition;

    // 접근 제어자 생략 (package-private)
    InitialWhenBuilder(BooleanSupplier currentCondition) {
      this.currentCondition = currentCondition;
    }

    // 이 시점에 제네릭 타입 T가 결정됩니다.
    public <T> ValueThenBuilder<T> then(Supplier<T> action) {
      List<ValueRule<T>> rules = new ArrayList<>();
      rules.add(new ValueRule<>(this.currentCondition, action));
      return new ValueThenBuilder<>(rules);
    }

    public <T> ValueThenBuilder<T> then(T value) {
      return then(() -> value);
    }
  }

  class ValueThenBuilder<T> {
    private final List<ValueRule<T>> rules;

    ValueThenBuilder(List<ValueRule<T>> rules) {
      this.rules = rules;
    }

    public ValueWhenBuilder<T> when(BooleanSupplier condition) {
      return new ValueWhenBuilder<>(this.rules, condition);
    }

    public ValueOtherBuilder<T> other(Supplier<T> action) {
      this.rules.add(new ValueRule<>(() -> true, action));
      return new ValueOtherBuilder<>(this.rules);
    }

    public ValueOtherBuilder<T> other(T value) {
      return other(() -> value);
    }

    // 지연 평가를 위한 build() 추가
    public CaseGet<T> build() {
      return new DefaultCaseGet<>(this.rules);
    }

    // 즉시 평가를 원할 경우를 위해 유지
    public T get() {
      return build().get();
    }
  }

  class ValueWhenBuilder<T> {
    private final List<ValueRule<T>> rules;
    private final BooleanSupplier currentCondition;

    ValueWhenBuilder(List<ValueRule<T>> rules, BooleanSupplier currentCondition) {
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

  class ValueOtherBuilder<T> {
    private final List<ValueRule<T>> rules;

    ValueOtherBuilder(List<ValueRule<T>> rules) {
      this.rules = rules;
    }

    // 지연 평가를 위한 build() 추가
    public CaseGet<T> build() {
      return new DefaultCaseGet<>(this.rules);
    }

    // 즉시 평가
    public T get() {
      return build().get();
    }
  }
}


// s : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
class DefaultCaseGet<T> implements CaseGet<T> {
  private final List<ValueRule<T>> rules;

  DefaultCaseGet(List<ValueRule<T>> rules) {
    // 불변 방어적 복사 적용
    this.rules = List.copyOf(rules);
  }

  @Override
  public T get() {
    for (ValueRule<T> rule : rules) {
      if (rule.condition.getAsBoolean()) {
        return rule.action.get();
      }
    }

    return null; // 매칭되는 조건이 없을 경우 null 반환 (기존 로직 유지)
  }
}


class ValueRule<T> {
  final BooleanSupplier condition;
  final Supplier<T> action;

  ValueRule(BooleanSupplier condition, Supplier<T> action) {
    this.condition = condition;
    this.action = action;
  }
}
// e : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
