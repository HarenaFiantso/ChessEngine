package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MoveTest {

  @Test
  void normalMovesExposeTheirJourney() {
    Move move = new Move.Normal(Square.G1, Square.F3);
    assertEquals(Square.G1, move.from());
    assertEquals(Square.F3, move.to());
  }

  @ParameterizedTest
  @EnumSource(Square.class)
  void movesMustLeaveTheirOrigin(Square square) {
    assertThrows(IllegalArgumentException.class, () -> new Move.Normal(square, square));
  }

  @Test
  void normalMovesRejectNullSquares() {
    assertThrows(NullPointerException.class, () -> new Move.Normal(null, Square.E4));
    assertThrows(NullPointerException.class, () -> new Move.Normal(Square.E4, null));
  }

  @ParameterizedTest
  @CsvSource({"E7,E8", "E7,D8", "A2,A1", "H2,G1"})
  void promotionsAcceptLastRankJourneys(Square from, Square to) {
    assertDoesNotThrow(() -> new Move.Promotion(from, to, PieceType.QUEEN));
  }

  @ParameterizedTest
  @CsvSource({"E6,E7", "E8,E7", "E3,E2", "E7,G8"})
  void promotionsRejectJourneysWithoutPromotionShape(Square from, Square to) {
    assertThrows(
        IllegalArgumentException.class, () -> new Move.Promotion(from, to, PieceType.QUEEN));
  }

  @ParameterizedTest
  @ValueSource(strings = {"PAWN", "KING"})
  void promotionsRejectImpossiblePieces(PieceType promoted) {
    assertThrows(
        IllegalArgumentException.class, () -> new Move.Promotion(Square.E7, Square.E8, promoted));
  }

  @ParameterizedTest
  @CsvSource({"E5,D6", "E5,F6", "D4,E3", "A4,B3"})
  void enPassantAcceptsSingleDiagonalStepsOntoTheTargetRanks(Square from, Square to) {
    assertDoesNotThrow(() -> new Move.EnPassant(from, to));
  }

  @ParameterizedTest
  @CsvSource({"E5,E6", "E4,D5", "A5,C6", "E2,D3"})
  void enPassantRejectsOtherJourneys(Square from, Square to) {
    assertThrows(IllegalArgumentException.class, () -> new Move.EnPassant(from, to));
  }

  @ParameterizedTest
  @CsvSource({"E1,G1", "E1,C1", "E8,G8", "E8,C8"})
  void castlingAcceptsTheFourKingPaths(Square from, Square to) {
    assertDoesNotThrow(() -> new Move.Castling(from, to));
  }

  @ParameterizedTest
  @CsvSource({"E1,F1", "E1,G8", "D1,B1", "E8,E1"})
  void castlingRejectsOtherJourneys(Square from, Square to) {
    assertThrows(IllegalArgumentException.class, () -> new Move.Castling(from, to));
  }
}
