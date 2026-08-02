package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DirectionTest {

  @ParameterizedTest
  @CsvSource({"NORTH,0,1", "SOUTH_EAST,1,-1", "WEST,-1,0", "NORTH_WEST,-1,1"})
  void deltasPointTheRightWay(Direction direction, int fileDelta, int rankDelta) {
    assertEquals(fileDelta, direction.fileDelta());
    assertEquals(rankDelta, direction.rankDelta());
  }

  @Test
  void orthogonalAndDiagonalPartitionTheCompass() {
    assertEquals(4, Direction.ORTHOGONAL.size());
    assertEquals(4, Direction.DIAGONAL.size());
    for (Direction direction : Direction.values()) {
      assertTrue(Direction.ORTHOGONAL.contains(direction) ^ Direction.DIAGONAL.contains(direction));
    }
  }
}
