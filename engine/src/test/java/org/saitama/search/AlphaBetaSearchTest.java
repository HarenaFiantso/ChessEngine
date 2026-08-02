package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;

class AlphaBetaSearchTest {

  private final SearchAlgorithm pruningOnly =
      new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false);
  private final SearchAlgorithm withTable = new AlphaBetaSearch(new ClassicalEvaluator());
  private final SearchAlgorithm exhaustive = new NegamaxSearch(new ClassicalEvaluator());

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, 2",
    "4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1, 2",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 0 1, 3",
    "4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1, 2",
    "7k/8/6Q1/8/8/8/8/6K1 w - - 0 1, 2",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 99 70, 2",
    "4k3/8/8/8/8/8/8/4RK2 b - - 0 1, 2"
  })
  void pruningAloneMatchesTheExhaustiveScoreExactly(String record, int depth) {
    SearchResult pruned = pruningOnly.search(Fen.parse(record), depth);
    SearchResult full = exhaustive.search(Fen.parse(record), depth);
    assertEquals(full.score(), pruned.score());
  }

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, 2",
    "4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1, 2"
  })
  void visitsFarFewerNodesThanTheExhaustiveSearch(String record, int depth) {
    SearchResult pruned = pruningOnly.search(Fen.parse(record), depth);
    SearchResult full = exhaustive.search(Fen.parse(record), depth);
    assertTrue(pruned.nodes() * 2 < full.nodes());
  }

  @ParameterizedTest
  @CsvSource({
    "6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1, 2, 999999",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 0 1, 3, 999997"
  })
  void findsForcedMatesWithExactScoresDespiteCaching(String record, int depth, int expected) {
    assertEquals(expected, withTable.search(Fen.parse(record), depth).score());
  }

  @Test
  void nullMovePruningCutsNodesAtEqualDepthWithoutChangingTheAnswer() {
    String kiwipete = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    SearchResult speculative =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), true)
            .search(Fen.parse(kiwipete), 5);
    SearchResult exact =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false)
            .search(Fen.parse(kiwipete), 5);
    assertTrue(speculative.nodes() * 3 < exact.nodes() * 2);
    assertEquals(exact.bestMove(), speculative.bestMove());
  }

  @Test
  void findsDeepForcedMatesWithAllPruningOn() {
    SearchResult result = withTable.search(Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1"), 5);
    assertEquals(Scores.MATE - 3, result.score());
  }

  @Test
  void transpositionsReduceSearchEffort() {
    SearchResult remembered = withTable.search(Fen.parse(Fen.STARTING), 4);
    SearchResult recomputed = pruningOnly.search(Fen.parse(Fen.STARTING), 4);
    assertTrue(remembered.nodes() < recomputed.nodes());
  }

  @Test
  void repeatedSearchesStayConsistent() {
    SearchResult first = withTable.search(Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1"), 3);
    SearchResult second = withTable.search(Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1"), 3);
    assertEquals(first.score(), second.score());
    assertEquals(first.bestMove(), second.bestMove());
    assertTrue(second.nodes() <= first.nodes());
  }

  @ParameterizedTest
  @CsvSource({"1", "2"})
  void declinesThePoisonedPawn(int depth) {
    SearchResult result = withTable.search(Fen.parse("4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1"), depth);
    assertTrue(result.bestMove().isPresent());
    assertNotEquals(new Move.Normal(Square.D1, Square.D5), result.bestMove().orElseThrow());
  }

  @Test
  void rootWindowsThatHitReturnTheFullWindowAnswer() {
    String record = "4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1";
    AlphaBetaSearch full =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false);
    AlphaBetaSearch windowed =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false);
    SearchResult trusted = full.search(Fen.parse(record), 3, AlphaBetaSearchTest::neverStop);
    SearchResult aspired =
        windowed.search(
            Fen.parse(record),
            3,
            AlphaBetaSearchTest::neverStop,
            trusted.score() - 50,
            trusted.score() + 50);
    assertEquals(trusted.score(), aspired.score());
    assertEquals(trusted.bestMove(), aspired.bestMove());
  }

  @Test
  void rootWindowsBelowTheTruthFailHigh() {
    AlphaBetaSearch search =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false);
    SearchResult result =
        search.search(
            Fen.parse("6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1"),
            2,
            AlphaBetaSearchTest::neverStop,
            -100,
            100);
    assertTrue(result.score() >= 100);
  }

  @Test
  void rootWindowsAboveTheTruthFailLow() {
    AlphaBetaSearch search =
        new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled(), false);
    SearchResult result =
        search.search(
            Fen.parse("3rk3/8/8/8/8/8/8/4K3 w - - 0 1"),
            2,
            AlphaBetaSearchTest::neverStop,
            500,
            600);
    assertTrue(result.score() <= 500);
  }

  private static boolean neverStop() {
    return false;
  }

  @Test
  void firingStopSignalsUnwindTheSearch() {
    AlphaBetaSearch search = new AlphaBetaSearch(new ClassicalEvaluator());
    assertThrows(SearchAborted.class, () -> search.search(Fen.parse(Fen.STARTING), 4, () -> true));
  }

  @Test
  void silentStopSignalsChangeNothing() {
    AlphaBetaSearch search = new AlphaBetaSearch(new ClassicalEvaluator());
    SearchResult limited = search.search(Fen.parse(Fen.STARTING), 2, () -> false);
    SearchResult plain =
        new AlphaBetaSearch(new ClassicalEvaluator()).search(Fen.parse(Fen.STARTING), 2);
    assertEquals(plain.score(), limited.score());
    assertEquals(plain.bestMove(), limited.bestMove());
  }
}
