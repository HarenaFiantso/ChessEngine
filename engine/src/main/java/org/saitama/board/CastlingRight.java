package org.saitama.board;

/** One of the four castling permissions a position can retain. */
public enum CastlingRight {
  WHITE_KINGSIDE('K'),
  WHITE_QUEENSIDE('Q'),
  BLACK_KINGSIDE('k'),
  BLACK_QUEENSIDE('q');

  private final char symbol;

  CastlingRight(char symbol) {
    this.symbol = symbol;
  }

  /** Returns the FEN symbol of this right, following the same case convention as pieces. */
  public char symbol() {
    return symbol;
  }
}
