package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ColorTest {

  @Test
  void whiteAndBlackOpposeEachOther() {
    assertSame(Color.BLACK, Color.WHITE.opposite());
    assertSame(Color.WHITE, Color.BLACK.opposite());
  }

  @ParameterizedTest
  @EnumSource(Color.class)
  void oppositeIsAnInvolution(Color color) {
    assertSame(color, color.opposite().opposite());
  }
}
