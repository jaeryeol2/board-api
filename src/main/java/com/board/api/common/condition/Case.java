package com.board.api.common.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public interface Case {

  // --- 실행 메서드 ---
  void execute();

  // --- 진입점 (static 메서드로 선언하여 인스턴스에서 호출 불가) ---
  static InitialWhenBuilder when(BooleanSupplier condition) {
    return new InitialWhenBuilder(condition);
  }

  static InitialWhenBuilder when(boolean condition) {
    return new InitialWhenBuilder(() -> condition);
  }

  // --- Builders (인터페이스 내부의 class는 자동으로 public static이 됩니다) ---
  class InitialWhenBuilder {
    private final BooleanSupplier currentCondition;

    // 외부에서 new로 생성하지 못하도록 접근 제어자 생략 (package-private)
    InitialWhenBuilder(BooleanSupplier currentCondition) {
      this.currentCondition = currentCondition;
    }

    public ActionThenBuilder then(Runnable action) {
      List<ActionRule> rules = new ArrayList<>();
      rules.add(new ActionRule(this.currentCondition, action));
      return new ActionThenBuilder(rules);
    }
  }

  class ActionThenBuilder {
    private final List<ActionRule> rules;

    ActionThenBuilder(List<ActionRule> rules) {
      this.rules = rules;
    }

    public ActionWhenBuilder when(BooleanSupplier condition) {
      return new ActionWhenBuilder(this.rules, condition);
    }

    public ActionWhenBuilder when(boolean condition) {
      return new ActionWhenBuilder(this.rules, () -> condition);
    }

    public ActionOtherBuilder other(Runnable action) {
      this.rules.add(new ActionRule(() -> true, action));
      return new ActionOtherBuilder(this.rules);
    }

    public Case build() {
      return new DefaultCase(this.rules);
    }

    public void execute() {
      build().execute();
    }
  }

  class ActionWhenBuilder {
    private final List<ActionRule> rules;
    private final BooleanSupplier currentCondition;

    ActionWhenBuilder(List<ActionRule> rules, BooleanSupplier currentCondition) {
      this.rules = rules;
      this.currentCondition = currentCondition;
    }

    public ActionThenBuilder then(Runnable action) {
      this.rules.add(new ActionRule(this.currentCondition, action));
      return new ActionThenBuilder(this.rules);
    }
  }

  class ActionOtherBuilder {
    private final List<ActionRule> rules;

    ActionOtherBuilder(List<ActionRule> rules) {
      this.rules = rules;
    }

    public Case build() {
      return new DefaultCase(this.rules);
    }

    public void execute() {
      new ActionThenBuilder(this.rules).execute();
    }
  }
}


// s : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
class DefaultCase implements Case {
  private final List<ActionRule> rules;

  DefaultCase(List<ActionRule> rules) {
    this.rules = List.copyOf(rules);
  }

  @Override
  public void execute() {
    for (ActionRule rule : rules) {
      if (rule.condition.getAsBoolean()) {
        rule.action.run();
        break;
      }
    }
  }
}


class ActionRule {
  final BooleanSupplier condition;
  final Runnable action;

  ActionRule(BooleanSupplier condition, Runnable action) {
    this.condition = condition;
    this.action = action;
  }
}
// e : package-private class : 파일 내부에서만 접근가능한 클래스(다른파일에서 접근 못함.)
