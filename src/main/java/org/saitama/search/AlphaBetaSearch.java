package org.saitama.search;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.evaluation.Evaluator;
import org.saitama.rules.Attacks;
import org.saitama.rules.MoveGenerator;

/**
 * Negamax with alpha-beta pruning: identical answers to the exhaustive search from a fraction of
 * the tree.
 *
 * <p>The window carries what both sides are already guaranteed elsewhere: alpha is the score the
 * mover can force, beta the score the opponent will allow. A move refuted by one reply scoring at
 * or above beta needs no further replies examined, because the opponent will simply avoid the line.
 * Pruning soundness is proven in tests by comparison against {@link NegamaxSearch}, never assumed.
 * Instances are not thread-safe; each search runs on one thread.
 */
public final class AlphaBetaSearch implements SearchAlgorithm {

  private static final int FIFTY_MOVE_HALFMOVE_LIMIT = 100;

  private final Evaluator evaluator;
  private long nodes;

  /** Creates a search judging leaves with {@code evaluator}. */
  public AlphaBetaSearch(Evaluator evaluator) {
    this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
  }

  @Override
  public SearchResult search(Position position, int depth) {
    Objects.requireNonNull(position, "position");
    if (depth < 1) {
      throw new IllegalArgumentException("Search depth starts at one: " + depth);
    }
    nodes = 0;
    Optional<Move> bestMove = Optional.empty();
    int bestScore = -Scores.INFINITY;
    for (Move move : MoveOrdering.byPromise(position, MoveGenerator.legalMoves(position))) {
      int score = -alphaBeta(position.apply(move), depth - 1, -Scores.INFINITY, -bestScore, 1);
      if (score > bestScore) {
        bestScore = score;
        bestMove = Optional.of(move);
      }
    }
    if (bestMove.isEmpty()) {
      bestScore = Attacks.isInCheck(position) ? -Scores.MATE : 0;
    }
    return new SearchResult(bestMove, bestScore, nodes);
  }

  private int alphaBeta(Position position, int depth, int alpha, int beta, int ply) {
    if (depth == 0) {
      return quiescence(position, alpha, beta, ply);
    }
    nodes++;
    List<Move> moves = MoveGenerator.legalMoves(position);
    if (moves.isEmpty()) {
      return Attacks.isInCheck(position) ? -(Scores.MATE - ply) : 0;
    }
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return 0;
    }

    int best = -Scores.INFINITY;
    for (Move move : MoveOrdering.byPromise(position, moves)) {
      best = Math.max(best, -alphaBeta(position.apply(move), depth - 1, -beta, -alpha, ply + 1));
      if (best >= beta) {
        return best;
      }
      alpha = Math.max(alpha, best);
    }
    return best;
  }

  private int quiescence(Position position, int alpha, int beta, int ply) {
    nodes++;
    List<Move> moves = MoveGenerator.legalMoves(position);
    if (moves.isEmpty()) {
      return Attacks.isInCheck(position) ? -(Scores.MATE - ply) : 0;
    }
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return 0;
    }
    boolean inCheck = Attacks.isInCheck(position);
    int best;
    if (inCheck) {
      best = -Scores.INFINITY;
    } else {
      best = evaluator.evaluate(position);
      if (best >= beta) {
        return best;
      }
      alpha = Math.max(alpha, best);
    }
    List<Move> candidates =
        inCheck
            ? moves
            : moves.stream().filter(move -> MoveOrdering.isCapture(position, move)).toList();
    for (Move move : MoveOrdering.byPromise(position, candidates)) {
      best = Math.max(best, -quiescence(position.apply(move), -beta, -alpha, ply + 1));
      if (best >= beta) {
        return best;
      }
      alpha = Math.max(alpha, best);
    }
    return best;
  }
}
