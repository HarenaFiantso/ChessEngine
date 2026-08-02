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
 * Plain minimax in its negamax formulation: every node maximizes its own mover-relative score,
 * which is the negation of the child's, so one code path serves both players.
 *
 * <p>The tree is explored exhaustively, which makes this algorithm the reference implementation
 * that pruning variants are checked against, and far too slow to play with beyond shallow depths.
 * Leaves are judged through the same capture-resolving quiescence policy as the pruning search, so
 * the two remain score-identical and differ only in how much of the tree they visit. Instances are
 * not thread-safe; each search runs on one thread.
 */
public final class NegamaxSearch implements SearchAlgorithm {

  private static final int FIFTY_MOVE_HALFMOVE_LIMIT = 100;

  private final Evaluator evaluator;
  private long nodes;

  /** Creates a search judging leaves with {@code evaluator}. */
  public NegamaxSearch(Evaluator evaluator) {
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
    for (Move move : MoveGenerator.legalMoves(position)) {
      int score = -negamax(position.apply(move), depth - 1, 1);
      if (score > bestScore) {
        bestScore = score;
        bestMove = Optional.of(move);
      }
    }
    if (bestMove.isEmpty()) {
      bestScore = Attacks.isInCheck(position) ? -Scores.MATE : 0;
    }
    return new SearchResult(bestMove, bestScore, nodes, depth);
  }

  private int negamax(Position position, int depth, int ply) {
    if (depth == 0) {
      return quiescence(position, ply);
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
    for (Move move : moves) {
      best = Math.max(best, -negamax(position.apply(move), depth - 1, ply + 1));
    }
    return best;
  }

  private int quiescence(Position position, int ply) {
    nodes++;
    List<Move> moves = MoveGenerator.legalMoves(position);
    if (moves.isEmpty()) {
      return Attacks.isInCheck(position) ? -(Scores.MATE - ply) : 0;
    }
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return 0;
    }
    boolean inCheck = Attacks.isInCheck(position);
    int best = inCheck ? -Scores.INFINITY : evaluator.evaluate(position);
    for (Move move : moves) {
      if (inCheck || MoveOrdering.isCapture(position, move)) {
        best = Math.max(best, -quiescence(position.apply(move), ply + 1));
      }
    }
    return best;
  }
}
