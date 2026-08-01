package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;

class IterativeDeepeningSearchTest {

  private static IterativeDeepeningSearch deepening() {
    return new IterativeDeepeningSearch(new AlphaBetaSearch(new ClassicalEvaluator()));
  }

  @Test
  void depthLimitedDeepeningMatchesTheDirectSearch() {
    SearchResult deepened = deepening().search(Fen.parse(Fen.STARTING), SearchLimits.depth(3));
    SearchResult direct =
        new AlphaBetaSearch(new ClassicalEvaluator()).search(Fen.parse(Fen.STARTING), 3);
    assertEquals(direct.score(), deepened.score());
    assertEquals(3, deepened.depth());
  }

  @Test
  void findsForcedMatesAndStopsDeepening() {
    SearchResult result =
        deepening().search(Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1"), SearchLimits.depth(12));
    assertEquals(Scores.MATE - 3, result.score());
    assertTrue(result.depth() <= 4);
  }

  @Test
  void zeroBudgetsStillReturnTheDepthOneMove() {
    SearchResult result =
        deepening().search(Fen.parse(Fen.STARTING), SearchLimits.moveTime(Duration.ZERO));
    assertTrue(result.bestMove().isPresent());
    assertEquals(1, result.depth());
  }

  @Test
  void anExpiringClockStopsTheDeepening() {
    AtomicLong fakeNanos = new AtomicLong();
    IterativeDeepeningSearch search =
        new IterativeDeepeningSearch(
            new AlphaBetaSearch(new ClassicalEvaluator()),
            () -> fakeNanos.addAndGet(Duration.ofMillis(1).toNanos()));
    SearchResult result =
        search.search(Fen.parse(Fen.STARTING), SearchLimits.moveTime(Duration.ofMillis(5)));
    assertTrue(result.bestMove().isPresent());
    assertTrue(result.depth() <= 2);
  }

  @Test
  void limitsRejectSenselessRequests() {
    assertThrows(IllegalArgumentException.class, () -> SearchLimits.depth(0));
    assertThrows(
        IllegalArgumentException.class, () -> SearchLimits.moveTime(Duration.ofMillis(-1)));
    assertThrows(
        IllegalArgumentException.class,
        () -> new SearchLimits(java.util.OptionalInt.empty(), java.util.Optional.empty()));
  }
}
