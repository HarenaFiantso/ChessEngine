package org.saitama.search;

import java.util.Optional;
import java.util.OptionalInt;
import org.saitama.board.Move;

/**
 * A move recommendation, the centipawn score backing it, and the number of nodes visited to find
 * it.
 *
 * <p>The best move is empty exactly when the searched position has no legal moves. The node count
 * exists so pruning improvements can prove, not claim, that they visit less of the tree.
 *
 * @param bestMove the strongest move found, empty in checkmate and stalemate positions
 * @param score the score of {@code bestMove} from the side to move's point of view
 * @param nodes how many positions the search visited
 * @param depth the deepest fully searched horizon backing {@code bestMove}
 */
public record SearchResult(Optional<Move> bestMove, int score, long nodes, int depth) {

  /**
   * Returns the signed mate distance in plies when the score announces a forced mate: positive when
   * the mover mates, negative when the mover gets mated, empty for ordinary scores.
   */
  public OptionalInt mateDistance() {
    if (score > Scores.MATE_THRESHOLD) {
      return OptionalInt.of(Scores.MATE - score);
    }
    if (score < -Scores.MATE_THRESHOLD) {
      return OptionalInt.of(-(Scores.MATE + score));
    }
    return OptionalInt.empty();
  }
}
