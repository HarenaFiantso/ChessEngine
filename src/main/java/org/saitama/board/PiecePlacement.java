package org.saitama.board;

import java.util.Optional;

/** Read access to an arrangement of pieces, however the implementation stores or mutates it. */
public interface PiecePlacement {

  /** Returns the piece standing on {@code square}, or empty if the square is vacant. */
  Optional<Piece> pieceOn(Square square);

  /** Returns the square of {@code side}'s king, or empty if that king is off the board. */
  Optional<Square> kingSquare(Color side);
}
