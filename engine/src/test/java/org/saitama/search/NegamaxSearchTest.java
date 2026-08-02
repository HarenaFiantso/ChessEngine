package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;

class NegamaxSearchTest {

  private final SearchAlgorithm search = new NegamaxSearch(new ClassicalEvaluator());

  @Test
  void findsMateInOne() {
    SearchResult result = search.search(Fen.parse("6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1"), 2);
    assertEquals(Optional.of(new Move.Normal(Square.A1, Square.A8)), result.bestMove());
    assertEquals(Scores.MATE - 1, result.score());
  }

  @Test
  void capturesTheFreeQueen() {
    SearchResult result = search.search(Fen.parse("4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1"), 2);
    assertEquals(Optional.of(new Move.Normal(Square.D1, Square.D5)), result.bestMove());
    assertTrue(result.score() > 300);
  }

  @Test
  void findsMateInTwoWithTheLadder() {
    SearchResult result = search.search(Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1"), 3);
    assertEquals(Scores.MATE - 3, result.score());
  }

  @Test
  void reportsCheckmateAgainstTheMover() {
    SearchResult result =
        search.search(
            Fen.parse("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3"), 2);
    assertEquals(Optional.empty(), result.bestMove());
    assertEquals(-Scores.MATE, result.score());
  }

  @Test
  void countsVisitedNodes() {
    SearchResult result = search.search(Fen.parse(Fen.STARTING), 2);
    assertTrue(result.nodes() > 20);
  }

  @Test
  void rejectsInvalidArguments() {
    assertThrows(NullPointerException.class, () -> search.search(null, 2));
    assertThrows(IllegalArgumentException.class, () -> search.search(Fen.parse(Fen.STARTING), 0));
    assertThrows(NullPointerException.class, () -> new NegamaxSearch(null));
  }
}
