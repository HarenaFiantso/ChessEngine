package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
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

  @ParameterizedTest
  @CsvSource({"E4,1,1,F5", "E4,-2,1,C5", "A1,7,7,H8", "D6,0,-3,D3"})
  void translatedReachesSquaresOnTheBoard(
      Square origin, int fileDelta, int rankDelta, Square expected) {
    assertEquals(Optional.of(expected), origin.translated(fileDelta, rankDelta));
  }

  @ParameterizedTest
  @CsvSource({"A1,-1,0", "A1,0,-1", "H8,1,0", "H8,0,1", "E4,4,0", "E4,0,5"})
  void translatedReturnsEmptyOffTheBoard(Square origin, int fileDelta, int rankDelta) {
    assertEquals(Optional.empty(), origin.translated(fileDelta, rankDelta));
  }

  @Test
  void neighborFollowsTheCompass() {
    assertEquals(Optional.of(Square.E5), Square.E4.neighbor(Direction.NORTH));
    assertEquals(Optional.of(Square.D3), Square.E4.neighbor(Direction.SOUTH_WEST));
    assertEquals(Optional.empty(), Square.H4.neighbor(Direction.EAST));
  }

  @ParameterizedTest
  @CsvSource({"A1,false", "H1,true", "A8,true", "H8,false", "E4,true", "D4,false"})
  void classifiesSquareShade(Square square, boolean light) {
    assertEquals(light, square.isLight());
  }
}
