package org.saitama.search;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * What may stop a search: a depth ceiling, a time budget, or both.
 *
 * @param maxDepth deepest iteration to complete, if bounded
 * @param maxMoveTime wall-clock budget for the whole search, if bounded
 */
public record SearchLimits(OptionalInt maxDepth, Optional<Duration> maxMoveTime) {

  /** Validates that at least one limit exists and that present limits are sensible. */
  public SearchLimits {
    Objects.requireNonNull(maxDepth, "maxDepth");
    Objects.requireNonNull(maxMoveTime, "maxMoveTime");
    if (maxDepth.isEmpty() && maxMoveTime.isEmpty()) {
      throw new IllegalArgumentException("A search needs at least one limit");
    }
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
}
