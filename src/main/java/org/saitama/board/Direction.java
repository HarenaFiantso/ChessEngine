package org.saitama.board;

import java.util.List;

/** One of the eight compass directions a ray can follow across the board, from white's view. */
public enum Direction {
  NORTH(0, 1),
  NORTH_EAST(1, 1),
  EAST(1, 0),
  SOUTH_EAST(1, -1),
  SOUTH(0, -1),
  SOUTH_WEST(-1, -1),
  WEST(-1, 0),
  NORTH_WEST(-1, 1);

  /** The four directions rooks slide along. */
  public static final List<Direction> ORTHOGONAL = List.of(NORTH, EAST, SOUTH, WEST);

  /** The four directions bishops slide along. */
  public static final List<Direction> DIAGONAL =
      List.of(NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST);

  private final int fileDelta;
  private final int rankDelta;

  Direction(int fileDelta, int rankDelta) {
    this.fileDelta = fileDelta;
    this.rankDelta = rankDelta;
  }

  /** Returns how many files east one step moves, negative for west. */
  public int fileDelta() {
    return fileDelta;
  }

  /** Returns how many ranks north one step moves, negative for south. */
  public int rankDelta() {
    return rankDelta;
  }
}
