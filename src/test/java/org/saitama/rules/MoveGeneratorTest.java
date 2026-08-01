package org.saitama.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

class MoveGeneratorTest {

  private static List<Move> movesFrom(String record, Square from) {
    return MoveGenerator.legalMoves(Fen.parse(record)).stream()
        .filter(move -> move.from() == from)
        .toList();
  }

  @ParameterizedTest
  @CsvSource({
    "4k3/8/8/8/4N3/8/8/4K3 w - - 0 1, E4, 8",
    "4k3/8/8/8/8/8/8/N3K3 w - - 0 1, A1, 2",
    "4k3/8/8/8/8/2P1P3/8/1N2K3 w - - 0 1, B1, 2",
    "4k3/8/8/8/4n3/8/8/4R1K1 b - - 0 1, E4, 0"
  })
  void generatesKnightMoves(String record, Square from, int expectedCount) {
    assertEquals(expectedCount, movesFrom(record, from).size());
  }

  @Test
  void knightsCaptureEnemiesButNotFriends() {
    List<Move> moves = movesFrom("4k3/8/8/8/8/2p1P3/8/1N2K3 w - - 0 1", Square.B1);
    assertTrue(moves.contains(new Move.Normal(Square.B1, Square.C3)));
    assertTrue(moves.contains(new Move.Normal(Square.B1, Square.A3)));
    assertEquals(3, moves.size());
  }

  @Test
  void rejectsNullPositions() {
    assertThrows(NullPointerException.class, () -> MoveGenerator.legalMoves(null));
  }

  @ParameterizedTest
  @CsvSource({
    "4k3/8/8/8/8/8/8/4K3 w - - 0 1, E1, 5",
    "4k3/8/8/8/8/8/8/K7 w - - 0 1, A1, 3",
    "8/8/8/3k4/8/3K4/8/8 w - - 0 1, D3, 5",
    "4k3/8/8/8/8/8/4r3/4K3 w - - 0 1, E1, 3"
  })
  void generatesKingMoves(String record, Square from, int expectedCount) {
    assertEquals(expectedCount, movesFrom(record, from).size());
  }

  @ParameterizedTest
  @CsvSource({
    "4k3/8/8/8/4R3/8/8/4K3 w - - 0 1, E4, 13",
    "4k3/8/8/8/4B3/8/8/4K3 w - - 0 1, E4, 13",
    "4k3/8/8/8/4Q3/8/8/4K3 w - - 0 1, E4, 26",
    "k7/8/8/8/1p2R1P1/8/8/4K3 w - - 0 1, E4, 10",
    "4k3/8/8/1b6/8/3B4/4K3/8 w - - 0 1, D3, 2"
  })
  void generatesSliderMoves(String record, Square from, int expectedCount) {
    assertEquals(expectedCount, movesFrom(record, from).size());
  }
}
