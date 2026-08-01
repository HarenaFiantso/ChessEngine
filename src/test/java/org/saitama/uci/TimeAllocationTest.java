package org.saitama.uci;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class TimeAllocationTest {

  @Test
  void dividesTheClockAcrossAssumedRemainingMoves() {
    assertEquals(
        Duration.ofSeconds(2),
        TimeAllocation.budgetFor(Duration.ofSeconds(60), Duration.ZERO, OptionalInt.empty()));
  }

  @Test
  void honorsAnnouncedMovesToGo() {
    assertEquals(
        Duration.ofSeconds(6),
        TimeAllocation.budgetFor(Duration.ofSeconds(60), Duration.ZERO, OptionalInt.of(10)));
  }

  @Test
  void addsHalfTheIncrement() {
    assertEquals(
        Duration.ofSeconds(3),
        TimeAllocation.budgetFor(
            Duration.ofSeconds(60), Duration.ofSeconds(2), OptionalInt.empty()));
  }

  @Test
  void neverBetsMoreThanHalfTheClockOnOneMove() {
    assertEquals(
        Duration.ofMillis(500),
        TimeAllocation.budgetFor(Duration.ofSeconds(1), Duration.ofSeconds(30), OptionalInt.of(1)));
  }

  @Test
  void neverShrinksBelowTheMinimumBudget() {
    assertEquals(
        Duration.ofMillis(20),
        TimeAllocation.budgetFor(Duration.ofMillis(30), Duration.ZERO, OptionalInt.empty()));
  }
}
