package org.saitama.board;

/** Kind of chess piece, independent of the side that owns it. */
public enum PieceType {
  PAWN('P'),
  KNIGHT('N'),
  BISHOP('B'),
  ROOK('R'),
  QUEEN('Q'),
  KING('K');

  private final char letter;

  PieceType(char letter) {
    this.letter = letter;
  }

  /** Returns the uppercase letter naming this piece type in English notation. */
  public char letter() {
    return letter;
  }
}
