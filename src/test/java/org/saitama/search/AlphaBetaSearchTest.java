package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;

class AlphaBetaSearchTest {

  private final SearchAlgorithm pruning = new AlphaBetaSearch(new ClassicalEvaluator());
  private final SearchAlgorithm exhaustive = new NegamaxSearch(new ClassicalEvaluator());

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, 3",
    "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1, 2",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 0 1, 3",
    "4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1, 3"
  })
  void matchesTheExhaustiveScoreExactly(String record, int depth) {
    SearchResult pruned = pruning.search(Fen.parse(record), depth);
    SearchResult full = exhaustive.search(Fen.parse(record), depth);
    assertEquals(full.score(), pruned.score());
  }

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, 3",
    "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1, 2"
  })
  void visitsFarFewerNodesThanTheExhaustiveSearch(String record, int depth) {
    SearchResult pruned = pruning.search(Fen.parse(record), depth);
    SearchResult full = exhaustive.search(Fen.parse(record), depth);
    assertTrue(pruned.nodes() * 2 < full.nodes());
  }

  @ParameterizedTest
  @CsvSource({
    "6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1, 2, 999999",
    "7k/8/8/8/8/8/1R6/R5K1 w - - 0 1, 3, 999997"
  })
  void findsForcedMatesWithExactScores(String record, int depth, int expected) {
    assertEquals(expected, pruning.search(Fen.parse(record), depth).score());
  }
}
