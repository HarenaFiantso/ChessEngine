package org.saitama.search;

import java.util.Optional;
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
 */
public record SearchResult(Optional<Move> bestMove, int score, long nodes) {}
