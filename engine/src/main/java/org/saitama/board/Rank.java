package org.saitama.board;

import java.util.Objects;

/** Horizontal row of the board, numbered 1 nearest white through 8 nearest black. */
public enum Rank {
  ONE,
  TWO,
  THREE,
  FOUR,
  FIVE,
  SIX,
  SEVEN,
  EIGHT;

  private static final Rank[] VALUES = values();

  /** Returns the rank at {@code index}, where 0 is rank 1 and 7 is rank 8. */
  public static Rank of(int index) {
    return VALUES[Objects.checkIndex(index, VALUES.length)];
  }

  /** Returns the zero-based index of this rank, counted from white's back rank. */
  @SuppressWarnings("EnumOrdinal")
  public int index() {
    return ordinal();
  }

  /** Returns the digit naming this rank in algebraic notation. */
  public char digit() {
    return (char) ('1' + index());
  }
}
