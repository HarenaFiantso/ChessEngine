package org.saitama.search;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * What may stop a search: a depth ceiling, a time budget, both, or neither.
 *
 * <p>Limits with neither bound express the UCI {@code go infinite} contract: only an external stop
 * signal, or the deepening search's own internal depth ceiling, ends the search. Callers passing
 * unlimited limits without a stop signal accept searching to that ceiling.
 *
 * @param maxDepth deepest iteration to complete, if bounded
 * @param maxMoveTime wall-clock budget for the whole search, if bounded
 */
public record SearchLimits(OptionalInt maxDepth, Optional<Duration> maxMoveTime) {

  /** Validates that present limits are sensible. */
  public SearchLimits {
    Objects.requireNonNull(maxDepth, "maxDepth");
    Objects.requireNonNull(maxMoveTime, "maxMoveTime");
    if (maxDepth.isPresent() && maxDepth.getAsInt() < 1) {
      throw new IllegalArgumentException("Depth limits start at one: " + maxDepth.getAsInt());
    }
    if (maxMoveTime.isPresent() && maxMoveTime.get().isNegative()) {
      throw new IllegalArgumentException("Time budgets cannot be negative: " + maxMoveTime.get());
    }
  }

  /** Returns limits that stop after fully searching {@code depth}. */
  public static SearchLimits depth(int depth) {
    return new SearchLimits(OptionalInt.of(depth), Optional.empty());
  }

  /** Returns limits that stop when {@code moveTime} has elapsed. */
  public static SearchLimits moveTime(Duration moveTime) {
    return new SearchLimits(OptionalInt.empty(), Optional.of(moveTime));
  }

  /** Returns limits that stop at whichever of {@code depth} and {@code moveTime} comes first. */
  public static SearchLimits of(int depth, Duration moveTime) {
    return new SearchLimits(OptionalInt.of(depth), Optional.of(moveTime));
  }

  /** Returns limits that bind neither depth nor time, leaving the stop to an external signal. */
  public static SearchLimits unlimited() {
    return new SearchLimits(OptionalInt.empty(), Optional.empty());
  }
}
