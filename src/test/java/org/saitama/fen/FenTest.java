package org.saitama.fen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.board.Board;
import org.saitama.board.Piece;
import org.saitama.board.Square;

class FenTest {

  @ParameterizedTest
  @CsvSource({
    "e1,WHITE_KING",
    "d1,WHITE_QUEEN",
    "a1,WHITE_ROOK",
    "c1,WHITE_BISHOP",
    "g1,WHITE_KNIGHT",
    "e2,WHITE_PAWN",
    "e8,BLACK_KING",
    "d8,BLACK_QUEEN",
    "h8,BLACK_ROOK",
    "f8,BLACK_BISHOP",
    "b8,BLACK_KNIGHT",
    "e7,BLACK_PAWN"
  })
  void parsesTheStartingPlacement(String square, Piece piece) {
    Board board = Fen.parsePlacement(Fen.STARTING_PLACEMENT);
    assertEquals(Optional.of(piece), board.pieceOn(Square.ofAlgebraic(square)));
  }

  @Test
  void startingPlacementLeavesTheMiddleEmpty() {
    Board board = Fen.parsePlacement(Fen.STARTING_PLACEMENT);
    assertEquals(Optional.empty(), board.pieceOn(Square.E4));
    assertEquals(Optional.empty(), board.pieceOn(Square.D5));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",
        "8/8/8/8/8/8/8/8",
        "4k3/8/8/8/8/8/8/4K3",
        "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R"
      })
  void placementsRoundTripThroughParseAndWrite(String placement) {
    assertEquals(placement, Fen.writePlacement(Fen.parsePlacement(placement)));
  }

  @Test
  void writesBuilderAssembledBoards() {
    Board board =
        Board.builder().put(Square.E1, Piece.WHITE_KING).put(Square.E8, Piece.BLACK_KING).build();
    assertEquals("4k3/8/8/8/8/8/8/4K3", Fen.writePlacement(board));
  }

  @Test
  void writesTheEmptyBoard() {
    assertEquals("8/8/8/8/8/8/8/8", Fen.writePlacement(Board.empty()));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "8/8",
        "8/8/8/8/8/8/8/8/8",
        "8/8/8/8/8/8/8/",
        "9/8/8/8/8/8/8/8",
        "44/8/8/8/8/8/8/8",
        "7/8/8/8/8/8/8/8",
        "ppppppppp/8/8/8/8/8/8/8",
        "p7p/8/8/8/8/8/8/8",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNX",
        ""
      })
  void rejectsMalformedPlacements(String placement) {
    assertThrows(IllegalArgumentException.class, () -> Fen.parsePlacement(placement));
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> Fen.parsePlacement(null));
    assertThrows(NullPointerException.class, () -> Fen.writePlacement(null));
  }
}
