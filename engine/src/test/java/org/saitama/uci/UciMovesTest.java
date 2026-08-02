package org.saitama.uci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.board.Move;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

class UciMovesTest {

  @Test
  void formatsAllMoveKinds() {
    assertEquals("g1f3", UciMoves.format(new Move.Normal(Square.G1, Square.F3)));
    assertEquals("e1g1", UciMoves.format(new Move.Castling(Square.E1, Square.G1)));
    assertEquals("e5d6", UciMoves.format(new Move.EnPassant(Square.E5, Square.D6)));
    assertEquals(
        "e7e8q", UciMoves.format(new Move.Promotion(Square.E7, Square.E8, PieceType.QUEEN)));
  }

  @Test
  void parsingRecoversTheMoveKindFromThePosition() {
    Position castlingReady = Fen.parse("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
    assertTrue(UciMoves.parse("e1g1", castlingReady) instanceof Move.Castling);
    assertTrue(UciMoves.parse("e1e2", castlingReady) instanceof Move.Normal);
    Position enPassantReady = Fen.parse("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
    assertTrue(UciMoves.parse("e5d6", enPassantReady) instanceof Move.EnPassant);
    Position promotionReady = Fen.parse("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
    assertEquals(
        new Move.Promotion(Square.A7, Square.A8, PieceType.KNIGHT),
        UciMoves.parse("a7a8n", promotionReady));
  }

  @Test
  void pawnCapturesOntoOrdinarySquaresStayNormal() {
    Position position = Fen.parse("4k3/8/8/3p4/4P3/8/8/4K3 w - - 0 1");
    assertTrue(UciMoves.parse("e4d5", position) instanceof Move.Normal);
  }

  @ParameterizedTest
  @ValueSource(strings = {"e2", "e2e4x9", "e2e9", "i2e4", "e7e8x", "x7e8q"})
  void rejectsTextThatIsNotCoordinateNotation(String text) {
    Position start = Fen.parse(Fen.STARTING);
    assertThrows(IllegalArgumentException.class, () -> UciMoves.parse(text, start));
  }

  @Test
  void rejectsMovesFromEmptySquares() {
    Position start = Fen.parse(Fen.STARTING);
    assertThrows(IllegalArgumentException.class, () -> UciMoves.parse("e4e5", start));
  }
}
