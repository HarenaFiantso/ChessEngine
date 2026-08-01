package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class FileTest {

  @Test
  void filesRunQueensideToKingside() {
    assertEquals(0, File.A.index());
    assertEquals(7, File.H.index());
  }

  @ParameterizedTest
  @EnumSource(File.class)
  void indexRoundTripsThroughOf(File file) {
    assertSame(file, File.of(file.index()));
  }

  @ParameterizedTest
  @CsvSource({"A,a", "E,e", "H,h"})
  void lettersFollowAlgebraicNotation(File file, char letter) {
    assertEquals(letter, file.letter());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 8})
  void ofRejectsIndexesOutsideTheBoard(int index) {
    assertThrows(IndexOutOfBoundsException.class, () -> File.of(index));
  }
}
