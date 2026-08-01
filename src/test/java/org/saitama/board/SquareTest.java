package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class SquareTest {

  @Test
  void boardHasSixtyFourSquares() {
    assertEquals(64, Square.values().length);
  }

  @ParameterizedTest
  @CsvSource({"A1,0", "H1,7", "A8,56", "H8,63", "E4,28"})
  void indexesFollowLittleEndianRankFileOrder(Square square, int index) {
    assertEquals(index, square.index());
  }

  @Test
  void exposesItsCoordinates() {
    assertSame(File.E, Square.E4.file());
    assertSame(Rank.FOUR, Square.E4.rank());
  }

  @ParameterizedTest
  @EnumSource(Square.class)
  void coordinatesRoundTripThroughOf(Square square) {
    assertSame(square, Square.of(square.file(), square.rank()));
  }

  @ParameterizedTest
  @EnumSource(Square.class)
  void algebraicNamesRoundTrip(Square square) {
    assertSame(square, Square.ofAlgebraic(square.algebraic()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "e", "e44", "i4", "e9", "E4", "44", "ee", "4e"})
  void ofAlgebraicRejectsMalformedNames(String name) {
    assertThrows(IllegalArgumentException.class, () -> Square.ofAlgebraic(name));
  }

  @Test
  void ofAlgebraicRejectsNull() {
    assertThrows(NullPointerException.class, () -> Square.ofAlgebraic(null));
  }
}
