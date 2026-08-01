package org.saitama.evaluation;

import org.saitama.board.Position;

/**
 * Scores positions heuristically so search can compare them.
 *
 * <p>Scores are expressed in centipawns, one hundredth of a pawn's worth, and always from the side
 * to move's point of view: positive means the mover stands better. That orientation is the negamax
 * convention, letting search negate a child's score instead of tracking whose turn it is.
 */
public interface Evaluator {

  /** Returns the worth of {@code position} in centipawns, positive when the mover stands better. */
  int evaluate(Position position);
}
