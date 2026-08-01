package org.saitama.board;

/** Side of a chess game, also the color of the pieces that side commands. */
public enum Color {
  WHITE,
  BLACK;

  /** Returns the opposing side. */
  public Color opposite() {
    return this == WHITE ? BLACK : WHITE;
  }
}
