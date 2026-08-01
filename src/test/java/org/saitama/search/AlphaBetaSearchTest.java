package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
      new AlphaBetaSearch(new ClassicalEvaluator(), TranspositionTable.disabled());
  private final SearchAlgorithm withTable = new AlphaBetaSearch(new ClassicalEvaluator());
  private final SearchAlgorithm exhaustive = new NegamaxSearch(new ClassicalEvaluator());

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, 2",
    "4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1, 2",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 0 1, 3",
    "4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1, 2"
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
}
