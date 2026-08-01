package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.fen.Fen;

class PositionApplyTest {

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, E2, E4,"
        + " rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1, C7, C5,"
        + " rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
    "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2, G1, F3,"
        + " rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
    "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2, E4, D5,"
        + " rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2",
    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1, A1, B1," + " r3k2r/8/8/8/8/8/8/1R2K2R b Kkq - 1 1",
    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1, E1, E2," + " r3k2r/8/8/8/8/8/4K3/R6R b kq - 1 1",
    "r3k2r/8/8/7Q/8/8/8/4K3 w kq - 0 1, H5, H8," + " r3k2Q/8/8/8/8/8/8/4K3 b q - 0 1",
    "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1, H8, G8," + " r3k1r1/8/8/8/8/8/8/R3K2R w KQq - 1 2"
  })
  void normalMovesProduceTheExpectedPosition(String before, Square from, Square to, String after) {
    Position result = Fen.parse(before).apply(new Move.Normal(from, to));
    assertEquals(after, Fen.write(result));
  }

  @Test
  void rejectsMovingFromAnEmptySquare() {
    Position start = Fen.parse(Fen.STARTING);
    Move move = new Move.Normal(Square.E4, Square.E5);
    assertThrows(IllegalArgumentException.class, () -> start.apply(move));
  }

  @Test
  void rejectsMovingTheOpponentsPiece() {
    Position start = Fen.parse(Fen.STARTING);
    Move move = new Move.Normal(Square.E7, Square.E5);
    assertThrows(IllegalArgumentException.class, () -> start.apply(move));
  }

  @Test
  void rejectsCapturingFriendlyPieces() {
    Position start = Fen.parse(Fen.STARTING);
    Move move = new Move.Normal(Square.D1, Square.D2);
    assertThrows(IllegalArgumentException.class, () -> start.apply(move));
  }

  @Test
  void rejectsNullMoves() {
    Position start = Fen.parse(Fen.STARTING);
    assertThrows(NullPointerException.class, () -> start.apply(null));
  }

  @ParameterizedTest
  @CsvSource({
    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1, E1, G1," + " r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1",
    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1, E1, C1," + " r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1",
    "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1, E8, G8," + " r4rk1/8/8/8/8/8/8/R3K2R w KQ - 1 2",
    "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1, E8, C8," + " 2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2"
  })
  void castlingMovesKingAndRookTogether(String before, Square from, Square to, String after) {
    Position result = Fen.parse(before).apply(new Move.Castling(from, to));
    assertEquals(after, Fen.write(result));
  }

  @ParameterizedTest
  @CsvSource({
    "8/P3k3/8/8/8/8/8/4K3 w - - 0 1, A7, A8, QUEEN, Q7/4k3/8/8/8/8/8/4K3 b - - 0 1",
    "4k2r/6P1/8/8/8/8/8/4K3 w k - 0 1, G7, H8, KNIGHT, 4k2N/8/8/8/8/8/8/4K3 b - - 0 1",
    "4k3/8/8/8/8/8/p7/4K3 b - - 0 44, A2, A1, QUEEN, 4k3/8/8/8/8/8/8/q3K3 w - - 0 45"
  })
  void promotionsReplaceThePawnAndResetTheClock(
      String before, Square from, Square to, PieceType promoted, String after) {
    Position result = Fen.parse(before).apply(new Move.Promotion(from, to, promoted));
    assertEquals(after, Fen.write(result));
  }

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3, E5, D6,"
        + " rnbqkbnr/ppp1pppp/3P4/8/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3",
    "rnbqkbnr/pppp1ppp/8/8/3Pp3/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 3, E4, D3,"
        + " rnbqkbnr/pppp1ppp/8/8/8/3p4/PPP1PPPP/RNBQKBNR w KQkq - 0 4"
  })
  void enPassantRemovesThePawnBehindTheTarget(String before, Square from, Square to, String after) {
    Position result = Fen.parse(before).apply(new Move.EnPassant(from, to));
    assertEquals(after, Fen.write(result));
  }

  @Test
  void rejectsEnPassantWithoutMatchingTarget() {
    Position noTarget = Fen.parse("4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1");
    Move capture = new Move.EnPassant(Square.E5, Square.D6);
    assertThrows(IllegalArgumentException.class, () -> noTarget.apply(capture));
    Position otherTarget = Fen.parse("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
    Move elsewhere = new Move.EnPassant(Square.E5, Square.F6);
    assertThrows(IllegalArgumentException.class, () -> otherTarget.apply(elsewhere));
  }

  @Test
  void rejectsCastlingWithoutTheRook() {
    Position position = Fen.parse("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
    Move move = new Move.Castling(Square.E1, Square.G1);
    assertThrows(IllegalArgumentException.class, () -> position.apply(move));
  }

  @Test
  void rejectsCastlingByPiecesOtherThanTheKing() {
    Position position = Fen.parse("4k3/8/8/8/8/8/8/4Q2R w - - 0 1");
    Move move = new Move.Castling(Square.E1, Square.G1);
    assertThrows(IllegalArgumentException.class, () -> position.apply(move));
  }

  @Test
  void rejectsPromotionsByPiecesOtherThanPawns() {
    Position position = Fen.parse("4k3/4R3/8/8/8/8/8/4K3 w - - 0 1");
    Move move = new Move.Promotion(Square.E7, Square.E8, PieceType.QUEEN);
    assertThrows(IllegalArgumentException.class, () -> position.apply(move));
  }

  @Test
  void rejectsPromotionsInTheWrongDirection() {
    Position position = Fen.parse("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
    Move move = new Move.Promotion(Square.E2, Square.E1, PieceType.QUEEN);
    assertThrows(IllegalArgumentException.class, () -> position.apply(move));
  }
}
