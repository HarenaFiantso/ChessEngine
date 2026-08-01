package org.saitama.board;

import java.util.Optional;

/**
 * Read access to full game state: placement plus the facts rules need beyond the pieces. Both the
 * immutable {@link Position} and the engine's mutable working state implement it, so attack
 * detection and move generation serve either without copying.
 */
public interface PositionView extends PiecePlacement {

  /** Returns the side whose turn it is. */
  Color sideToMove();

  /** Returns the en passant target square, if the last move was a double push. */
  Optional<Square> enPassantTarget();

  /** Returns whether {@code right} is still retained. */
  boolean castlingAllowed(CastlingRight right);

  /** Returns halfmoves since the last capture or pawn advance. */
  int halfmoveClock();

  /** Returns the one-based move counter, incremented after black moves. */
  int fullmoveNumber();
}
