package org.saitama.uci;

import java.time.Duration;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Turns a game clock into a budget for one move.
 *
 * <p>The policy is deliberately plain: divide the remaining time by the moves the game is assumed
 * to still last, add half the increment, and never bet more than half the clock on a single move.
 * Refinements such as position-dependent budgets wait for evidence from real games.
 */
final class TimeAllocation {

  private static final int ASSUMED_MOVES_REMAINING = 30;
  private static final int INCREMENT_SHARE = 2;
  private static final int SINGLE_MOVE_CAP_DIVISOR = 2;
  private static final Duration MINIMUM_BUDGET = Duration.ofMillis(20);

  private TimeAllocation() {}

  static Duration budgetFor(Duration remaining, Duration increment, OptionalInt movesToGo) {
    Objects.requireNonNull(remaining, "remaining");
    Objects.requireNonNull(increment, "increment");
    int divisor = Math.max(1, movesToGo.orElse(ASSUMED_MOVES_REMAINING));
    Duration budget = remaining.dividedBy(divisor).plus(increment.dividedBy(INCREMENT_SHARE));
    Duration cap = remaining.dividedBy(SINGLE_MOVE_CAP_DIVISOR);
    if (budget.compareTo(cap) > 0) {
      budget = cap;
    }
    return budget.compareTo(MINIMUM_BUDGET) < 0 ? MINIMUM_BUDGET : budget;
  }
}
