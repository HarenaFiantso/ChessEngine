package org.saitama.rules;

/** Step offsets shared by attack detection and move generation that are not compass rays. */
final class Steps {

  static final int[][] KNIGHT_JUMPS = {
    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}
  };

  private Steps() {}
}
