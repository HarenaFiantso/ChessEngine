package org.saitama.fen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.board.Board;
import org.saitama.board.CastlingRights;
import org.saitama.board.Color;
import org.saitama.board.Piece;
import org.saitama.board.Position;
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
    assertThrows(NullPointerException.class, () -> Fen.parse(null));
    assertThrows(NullPointerException.class, () -> Fen.write(null));
  }

  @Test
  void parsesTheCompleteStartingRecord() {
    Position position = Fen.parse(Fen.STARTING);
    assertEquals(Fen.parsePlacement(Fen.STARTING_PLACEMENT), position.board());
    assertEquals(Color.WHITE, position.sideToMove());
    assertEquals(CastlingRights.all(), position.castlingRights());
    assertEquals(Optional.empty(), position.enPassantTarget());
    assertEquals(0, position.halfmoveClock());
    assertEquals(1, position.fullmoveNumber());
  }

  @Test
  void parsesMidgameStateFields() {
    Position position = Fen.parse("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2");
    assertEquals(Color.WHITE, position.sideToMove());
    assertEquals(Optional.of(Square.C6), position.enPassantTarget());
    assertEquals(0, position.halfmoveClock());
    assertEquals(2, position.fullmoveNumber());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
        "r3k2r/8/8/8/8/8/8/R3K2R b Kq - 10 25",
        "4k3/8/8/8/8/8/8/4K3 w - - 99 120"
      })
  void completeRecordsRoundTripThroughParseAndWrite(String record) {
    assertEquals(record, Fen.write(Fen.parse(record)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 extra",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -  0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR W KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QK - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KK - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkqq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w  - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e4 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq E6 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - -1 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 01 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - x 1"
      })
  void rejectsMalformedOrInconsistentRecords(String record) {
    assertThrows(IllegalArgumentException.class, () -> Fen.parse(record));
  }
}
