package org.saitama.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.board.Board;
import org.saitama.board.Color;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

class AttacksTest {

  @ParameterizedTest
  @CsvSource({
    "8/8/8/8/4P3/8/8/8, D5, WHITE, true",
    "8/8/8/8/4P3/8/8/8, F5, WHITE, true",
    "8/8/8/8/4P3/8/8/8, E5, WHITE, false",
    "8/8/8/8/4P3/8/8/8, D3, WHITE, false",
    "8/8/8/4p3/8/8/8/8, D4, BLACK, true",
    "8/8/8/4p3/8/8/8/8, F4, BLACK, true",
    "8/8/8/4p3/8/8/8/8, E4, BLACK, false",
    "8/8/8/8/P7/8/8/8, B5, WHITE, true",
    "8/8/8/8/4N3/8/8/8, D6, WHITE, true",
    "8/8/8/8/4N3/8/8/8, F2, WHITE, true",
    "8/8/8/8/4N3/8/8/8, C3, WHITE, true",
    "8/8/8/8/4N3/8/8/8, E5, WHITE, false",
    "8/8/3PPP2/3PNP2/3PPP2/8/8/8, D7, WHITE, true",
    "8/8/8/8/4B3/8/8/8, H7, WHITE, true",
    "8/8/8/8/4B3/8/8/8, A8, WHITE, true",
    "8/8/8/8/4B3/8/8/8, E5, WHITE, false",
    "8/8/2p5/8/4B3/8/8/8, C6, WHITE, true",
    "8/8/2p5/8/4B3/8/8/8, B7, WHITE, false",
    "8/8/2P5/8/4B3/8/8/8, C6, WHITE, true",
    "8/8/8/8/R7/8/8/8, A8, WHITE, true",
    "8/8/8/8/R7/8/8/8, H4, WHITE, true",
    "8/8/8/8/R7/8/8/8, B5, WHITE, false",
    "8/8/8/n7/R7/8/8/8, A5, WHITE, true",
    "8/8/8/n7/R7/8/8/8, A8, WHITE, false",
    "8/8/8/8/3q4/8/8/8, D1, BLACK, true",
    "8/8/8/8/3q4/8/8/8, G7, BLACK, true",
    "8/8/8/8/3q4/8/8/8, E6, BLACK, false",
    "8/8/8/8/4K3/8/8/8, E5, WHITE, true",
    "8/8/8/8/4K3/8/8/8, D3, WHITE, true",
    "8/8/8/8/4K3/8/8/8, E6, WHITE, false",
    "8/8/8/8/8/8/8/8, E4, WHITE, false"
  })
  void reportsWhetherTheSquareIsAttacked(
      String placement, Square square, Color attacker, boolean expected) {
    Board board = Fen.parsePlacement(placement);
    assertEquals(expected, Attacks.isAttacked(board, square, attacker));
  }

  @ParameterizedTest
  @CsvSource({"D5", "E5", "F5", "D4", "F4", "D3", "E3", "F3"})
  void queensAttackAllNeighbors(Square neighbor) {
    Board board = Fen.parsePlacement("8/8/8/8/4q3/8/8/8");
    assertTrue(Attacks.isAttacked(board, neighbor, Color.BLACK));
  }

  @Test
  void rejectsNullArguments() {
    Board board = Board.empty();
    assertThrows(
        NullPointerException.class, () -> Attacks.isAttacked(null, Square.E4, Color.WHITE));
    assertThrows(NullPointerException.class, () -> Attacks.isAttacked(board, null, Color.WHITE));
    assertThrows(NullPointerException.class, () -> Attacks.isAttacked(board, Square.E4, null));
  }

  @Test
  void theStartingPositionIsNotCheck() {
    assertFalse(Attacks.isInCheck(Fen.parse(Fen.STARTING)));
  }

  @ParameterizedTest
  @CsvSource({
    "rnb2bnr/ppppkppp/8/4Q3/4P3/8/PPPP1PPP/RNB1KBNR b KQ - 0 3, true",
    "4k3/4r3/8/8/8/8/4P3/4K3 w - - 0 1, false",
    "4k3/4r3/8/8/8/8/8/4K3 w - - 0 1, true",
    "4k3/8/8/8/8/3n4/8/4K3 w - - 0 1, true",
    "4k3/8/8/8/8/8/3p4/4K3 w - - 0 1, true",
    "4k3/8/8/8/8/8/8/4K3 w - - 0 1, false"
  })
  void reportsWhetherTheSideToMoveIsInCheck(String record, boolean expected) {
    assertEquals(expected, Attacks.isInCheck(Fen.parse(record)));
  }

  @Test
  void reportsCheckForEitherSideOnDemand() {
    Board board = Fen.parsePlacement("4k3/8/8/8/B7/8/8/4K3");
    assertTrue(Attacks.isInCheck(board, Color.BLACK));
    assertFalse(Attacks.isInCheck(board, Color.WHITE));
  }

  @Test
  void checkQueriesRequireTheKingOnTheBoard() {
    assertThrows(IllegalStateException.class, () -> Attacks.isInCheck(Board.empty(), Color.WHITE));
  }
}
