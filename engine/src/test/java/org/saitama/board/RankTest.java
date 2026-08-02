package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {

  @Test
  void ranksRunFromWhiteTowardBlack() {
    assertEquals(0, Rank.ONE.index());
    assertEquals(7, Rank.EIGHT.index());
  }

  @ParameterizedTest
  @EnumSource(Rank.class)
  void indexRoundTripsThroughOf(Rank rank) {
    assertSame(rank, Rank.of(rank.index()));
  }

  @ParameterizedTest
  @CsvSource({"ONE,1", "FOUR,4", "EIGHT,8"})
  void digitsFollowAlgebraicNotation(Rank rank, char digit) {
    assertEquals(digit, rank.digit());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 8})
  void ofRejectsIndexesOutsideTheBoard(int index) {
    assertThrows(IndexOutOfBoundsException.class, () -> Rank.of(index));
  }
}
