package org.saitama.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

class StaticExchangeTest {

  private static int see(String record, Move capture) {
    return StaticExchange.evaluate(Fen.parse(record), capture);
  }

  @Test
  void undefendedPiecesAreWonOutright() {
    assertEquals(
        100, see("k7/8/8/3p4/8/8/8/3RK3 w - - 0 1", new Move.Normal(Square.D1, Square.D5)));
  }

  @Test
  void equalTradesBalanceToZero() {
    assertEquals(
        0, see("k7/8/2p5/3p4/2P5/8/8/K7 w - - 0 1", new Move.Normal(Square.C4, Square.D5)));
  }

  @Test
  void takingDefendedPawnsWithTheQueenLosesHer() {
    assertEquals(
        -800, see("k7/8/2p5/3p4/8/8/8/3QK3 w - - 0 1", new Move.Normal(Square.D1, Square.D5)));
  }

  @Test
  void knightForPawnAgainstOneDefenderStaysLosing() {
    assertEquals(
        -220, see("k7/8/1n6/3p4/8/4N3/8/4K3 w - - 0 1", new Move.Normal(Square.E3, Square.D5)));
  }

  @Test
  void rooksBehindRooksJoinTheExchange() {
    assertEquals(
        100, see("3r3k/8/8/3p4/8/8/3R4/3RK3 w - - 0 1", new Move.Normal(Square.D2, Square.D5)));
  }

  @Test
  void kingsMayOnlyRecaptureUndefendedSquares() {
    assertEquals(
        100, see("8/8/4k3/3p4/2P1P3/8/8/K7 w - - 0 1", new Move.Normal(Square.C4, Square.D5)));
  }

  @Test
  void kingsRecaptureFreelyWhenNothingAnswers() {
    assertEquals(0, see("8/8/4k3/3p4/2P5/8/8/K7 w - - 0 1", new Move.Normal(Square.C4, Square.D5)));
  }

  @Test
  void enPassantCapturesValueThePawnBehindTheTarget() {
    assertEquals(
        100, see("k7/8/8/3pP3/8/8/8/K7 w - d6 0 1", new Move.EnPassant(Square.E5, Square.D6)));
  }

  @Test
  void quietMovesAreRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> see(Fen.STARTING, new Move.Normal(Square.E2, Square.E4)));
  }
}
