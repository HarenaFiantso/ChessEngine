package org.saitama.board;

import java.util.Objects;

/** Vertical column of the board, labeled 'a' through 'h' from white's queenside. */
public enum File {
  A,
  B,
  C,
  D,
  E,
  F,
  G,
  H;

  private static final File[] VALUES = values();

  /** Returns the file at {@code index}, where 0 is file a and 7 is file h. */
  public static File of(int index) {
    return VALUES[Objects.checkIndex(index, VALUES.length)];
  }

  /** Returns the zero-based index of this file, counted from file a. */
  @SuppressWarnings("EnumOrdinal")
  public int index() {
    return ordinal();
  }

  /** Returns the lowercase letter naming this file in algebraic notation. */
  public char letter() {
    return (char) ('a' + index());
  }
}
