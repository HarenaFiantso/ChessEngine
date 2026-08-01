package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PieceTypeTest {

  @Test
  void thereAreSixPieceTypes() {
    assertEquals(6, PieceType.values().length);
  }

  @ParameterizedTest
  @CsvSource({"PAWN,P", "KNIGHT,N", "BISHOP,B", "ROOK,R", "QUEEN,Q", "KING,K"})
  void lettersFollowEnglishNotation(PieceType type, char letter) {
    assertEquals(letter, type.letter());
  }
}
