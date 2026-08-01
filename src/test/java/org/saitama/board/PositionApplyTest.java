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
}
