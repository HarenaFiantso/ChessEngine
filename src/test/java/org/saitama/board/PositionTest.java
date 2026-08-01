package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PositionTest {

  private static Position position(Color sideToMove, Optional<Square> enPassantTarget) {
    return new Position(Board.empty(), sideToMove, CastlingRights.none(), enPassantTarget, 0, 1);
  }

  @Test
  void exposesItsComponents() {
    Position position = position(Color.WHITE, Optional.empty());
    assertEquals(Board.empty(), position.board());
    assertEquals(Color.WHITE, position.sideToMove());
    assertEquals(CastlingRights.none(), position.castlingRights());
    assertEquals(Optional.empty(), position.enPassantTarget());
    assertEquals(0, position.halfmoveClock());
    assertEquals(1, position.fullmoveNumber());
  }

  @Test
  void rejectsNegativeHalfmoveClock() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Position(
                Board.empty(), Color.WHITE, CastlingRights.none(), Optional.empty(), -1, 1));
  }

  @Test
  void rejectsFullmoveNumberBelowOne() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Position(
                Board.empty(), Color.WHITE, CastlingRights.none(), Optional.empty(), 0, 0));
  }

  @ParameterizedTest
  @CsvSource({"WHITE,e6", "BLACK,e3"})
  void acceptsEnPassantTargetsBehindTheJustMovedPawn(Color sideToMove, String target) {
    assertDoesNotThrow(() -> position(sideToMove, Optional.of(Square.ofAlgebraic(target))));
  }

  @ParameterizedTest
  @CsvSource({"WHITE,e3", "WHITE,e4", "BLACK,e6", "BLACK,c5"})
  void rejectsEnPassantTargetsOnImpossibleRanks(Color sideToMove, String target) {
    Optional<Square> square = Optional.of(Square.ofAlgebraic(target));
    assertThrows(IllegalArgumentException.class, () -> position(sideToMove, square));
  }

  @Test
  void rejectsNullComponents() {
    assertThrows(
        NullPointerException.class,
        () -> new Position(null, Color.WHITE, CastlingRights.none(), Optional.empty(), 0, 1));
    assertThrows(
        NullPointerException.class,
        () -> new Position(Board.empty(), null, CastlingRights.none(), Optional.empty(), 0, 1));
    assertThrows(
        NullPointerException.class,
        () -> new Position(Board.empty(), Color.WHITE, null, Optional.empty(), 0, 1));
    assertThrows(
        NullPointerException.class,
        () -> new Position(Board.empty(), Color.WHITE, CastlingRights.none(), null, 0, 1));
  }
}
