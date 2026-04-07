package com.board.api.common.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Case {
  private Case() {}

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

    // 실행(Action) 전용 체인으로 진입 (Runnable)
    public ActionThenBuilder then(Runnable action) {
      List<ActionRule> rules = new ArrayList<>();
      rules.add(new ActionRule(this.currentCondition, action));
      return new ActionThenBuilder(rules);
    }

  }

  private static class ActionRule {
    private final BooleanSupplier condition;
    private final Runnable action;

    private ActionRule(BooleanSupplier condition, Runnable action) {
      this.condition = condition;
      this.action = action;
    }
  }

  public static class ActionThenBuilder {
    private final List<ActionRule> rules;

    private ActionThenBuilder(List<ActionRule> rules) {
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

    public void execute() {
      for (ActionRule rule : rules) {
        if (rule.condition.getAsBoolean()) {
          rule.action.run();
          break;
        }
      }
    }
  }

  public static class ActionWhenBuilder {
    private final List<ActionRule> rules;
    private final BooleanSupplier currentCondition;

    private ActionWhenBuilder(List<ActionRule> rules, BooleanSupplier currentCondition) {
      this.rules = rules;
      this.currentCondition = currentCondition;
    }

    public ActionThenBuilder then(Runnable action) {
      this.rules.add(new ActionRule(this.currentCondition, action));
      return new ActionThenBuilder(this.rules);
    }
  }

  public static class ActionOtherBuilder {
    private final List<ActionRule> rules;

    private ActionOtherBuilder(List<ActionRule> rules) {
      this.rules = rules;
    }

    public void execute() {
      new ActionThenBuilder(this.rules).execute();
    }
  }

}
