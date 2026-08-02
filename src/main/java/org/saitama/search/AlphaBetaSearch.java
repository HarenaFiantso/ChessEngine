package org.saitama.search;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import org.saitama.board.Color;
import org.saitama.board.Move;
import org.saitama.board.MutablePosition;
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
 *
 * <p>The tree is walked on one {@link MutablePosition} scratch copy per search: each candidate is
 * made, tested for leaving its own king attacked, searched, and unmade, so no node pays for board
 * copies or full-board key recomputation. Because legality is discovered move by move, checkmate
 * and stalemate reveal themselves only after the loop finds nothing legal to play. An aborted
 * search abandons the scratch copy mid-line, which is safe because the next search starts from a
 * fresh copy. Instances are not thread-safe; each search runs on one thread.
 */
public final class AlphaBetaSearch implements SearchAlgorithm {

  private static final int FIFTY_MOVE_HALFMOVE_LIMIT = 100;

  private static final int DEFAULT_TABLE_CAPACITY = 1 << 16;

  private final Evaluator evaluator;
  private final TranspositionTable table;
  private long nodes;
  private BooleanSupplier stopSignal = AlphaBetaSearch::neverStop;

  /** Creates a search judging leaves with {@code evaluator} and a default transposition table. */
  public AlphaBetaSearch(Evaluator evaluator) {
    this(evaluator, TranspositionTable.withCapacity(DEFAULT_TABLE_CAPACITY));
  }

  /**
   * Creates a search judging leaves with {@code evaluator} and remembering nodes in {@code table}.
   */
  public AlphaBetaSearch(Evaluator evaluator, TranspositionTable table) {
    this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
    this.table = Objects.requireNonNull(table, "table");
  }

  @Override
  public SearchResult search(Position position, int depth) {
    return search(position, depth, AlphaBetaSearch::neverStop);
  }

  /**
   * Searches like {@link #search(Position, int)} but polls {@code stop} at every node.
   *
   * @throws SearchAborted if {@code stop} fires; the caller must discard this iteration
   */
  SearchResult search(Position position, int depth, BooleanSupplier stop) {
    Objects.requireNonNull(position, "position");
    this.stopSignal = Objects.requireNonNull(stop, "stop");
    if (depth < 1) {
      throw new IllegalArgumentException("Search depth starts at one: " + depth);
    }
    nodes = 0;
    MutablePosition current = MutablePosition.copyOf(position);
    Optional<Move> bestMove = Optional.empty();
    int bestScore = -Scores.INFINITY;
    for (Move move : MoveOrdering.byPromise(position, MoveGenerator.legalMoves(position))) {
      MutablePosition.Undo undo = current.make(move);
      int score = -alphaBeta(current, depth - 1, -Scores.INFINITY, -bestScore, 1);
      current.unmake(move, undo);
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

  private int alphaBeta(MutablePosition position, int depth, int alpha, int beta, int ply) {
    if (depth == 0) {
      return quiescence(position, alpha, beta, ply);
    }
    if (stopSignal.getAsBoolean()) {
      throw new SearchAborted();
    }
    nodes++;
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return fiftyMoveVerdict(position, ply);
    }
    long key = position.zobristKey();
    Optional<TranspositionTable.Entry> remembered = table.probe(key);
    if (remembered.isPresent() && remembered.get().depth() >= depth) {
      int cachedScore = fromTableScore(remembered.get().score(), ply);
      TranspositionTable.NodeType type = remembered.get().type();
      boolean usable =
          type == TranspositionTable.NodeType.EXACT
              || (type == TranspositionTable.NodeType.LOWER_BOUND && cachedScore >= beta)
              || (type == TranspositionTable.NodeType.UPPER_BOUND && cachedScore <= alpha);
      if (usable) {
        return cachedScore;
      }
    }
    int alphaAtEntry = alpha;
    int best = -Scores.INFINITY;
    Move bestMove = null;
    int legalMoves = 0;
    Color mover = position.sideToMove();
    Optional<Move> rememberedBest = remembered.flatMap(TranspositionTable.Entry::bestMove);
    List<Move> candidates = MoveGenerator.pseudoLegalMoves(position);
    for (Move move : MoveOrdering.byPromise(position, candidates, rememberedBest)) {
      MutablePosition.Undo undo = position.make(move);
      if (Attacks.isInCheck(position, mover)) {
        position.unmake(move, undo);
        continue;
      }
      legalMoves++;
      int score = -alphaBeta(position, depth - 1, -beta, -alpha, ply + 1);
      position.unmake(move, undo);
      if (score > best) {
        best = score;
        bestMove = move;
      }
      if (best >= beta) {
        break;
      }
      alpha = Math.max(alpha, best);
    }
    if (legalMoves == 0) {
      return Attacks.isInCheck(position) ? -(Scores.MATE - ply) : 0;
    }
    TranspositionTable.NodeType type =
        best >= beta
            ? TranspositionTable.NodeType.LOWER_BOUND
            : best <= alphaAtEntry
                ? TranspositionTable.NodeType.UPPER_BOUND
                : TranspositionTable.NodeType.EXACT;
    table.store(key, depth, toTableScore(best, ply), type, Optional.ofNullable(bestMove));
    return best;
  }

  private int quiescence(MutablePosition position, int alpha, int beta, int ply) {
    if (stopSignal.getAsBoolean()) {
      throw new SearchAborted();
    }
    nodes++;
    boolean inCheck = Attacks.isInCheck(position);
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return fiftyMoveVerdict(position, ply);
    }
    List<Move> pseudoLegal = MoveGenerator.pseudoLegalMoves(position);
    if (!inCheck && !hasLegalMove(position, pseudoLegal)) {
      return 0;
    }
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
            ? pseudoLegal
            : pseudoLegal.stream().filter(move -> MoveOrdering.isCapture(position, move)).toList();
    Color mover = position.sideToMove();
    int legalTried = 0;
    for (Move move : MoveOrdering.byPromise(position, candidates)) {
      MutablePosition.Undo undo = position.make(move);
      if (Attacks.isInCheck(position, mover)) {
        position.unmake(move, undo);
        continue;
      }
      legalTried++;
      int score = -quiescence(position, -beta, -alpha, ply + 1);
      position.unmake(move, undo);
      best = Math.max(best, score);
      if (best >= beta) {
        return best;
      }
      alpha = Math.max(alpha, best);
    }
    if (inCheck && legalTried == 0) {
      return -(Scores.MATE - ply);
    }
    return best;
  }

  /**
   * Settles a node whose halfmove clock has reached the fifty-move limit: a draw unless the mover
   * is checkmated, because mate outranks the clock.
   */
  private static int fiftyMoveVerdict(MutablePosition position, int ply) {
    if (!Attacks.isInCheck(position)
        || hasLegalMove(position, MoveGenerator.pseudoLegalMoves(position))) {
      return 0;
    }
    return -(Scores.MATE - ply);
  }

  private static boolean hasLegalMove(MutablePosition position, List<Move> pseudoLegalMoves) {
    Color mover = position.sideToMove();
    for (Move move : pseudoLegalMoves) {
      MutablePosition.Undo undo = position.make(move);
      boolean legal = !Attacks.isInCheck(position, mover);
      position.unmake(move, undo);
      if (legal) {
        return true;
      }
    }
    return false;
  }

  private static int toTableScore(int score, int ply) {
    if (score > Scores.MATE_THRESHOLD) {
      return score + ply;
    }
    if (score < -Scores.MATE_THRESHOLD) {
      return score - ply;
    }
    return score;
  }

  private static boolean neverStop() {
    return false;
  }

  private static int fromTableScore(int score, int ply) {
    if (score > Scores.MATE_THRESHOLD) {
      return score - ply;
    }
    if (score < -Scores.MATE_THRESHOLD) {
      return score + ply;
    }
    return score;
  }
}
