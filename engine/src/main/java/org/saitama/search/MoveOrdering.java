package org.saitama.search;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.PositionView;

/**
 * Orders moves so alpha-beta meets its best candidates first: the remembered best move, captures by
 * most valuable victim and least valuable attacker, promotions, then quiet moves ranked by how
 * recently and how often they have refuted other lines.
 *
 * <p>Captures order themselves by inspection, but quiet moves need memory, and this class keeps two
 * kinds. Killer moves are the last two quiet refuters at each ply: siblings at one ply face the
 * same threats, so a move that just refuted one of them often refutes the next. The history table
 * is global and statistical, crediting every quiet refuter's side, origin, and destination with the
 * square of the depth it refuted, so success at deep nodes counts for more. Both survive across
 * searches, like the transposition table, so ordering knowledge keeps compounding while positions
 * stay similar. Sorting is stable, and the ranks are relative worth only, deliberately independent
 * of the evaluator's centipawn policy. Instances are not thread-safe.
 */
final class MoveOrdering {

  private static final int REMEMBERED_BEST_BONUS = 2_000_000;
  private static final int CAPTURE_BASE = 1_000_000;
  private static final int PROMOTION_BASE = 900_000;
  private static final int FIRST_KILLER_BONUS = 800_000;
  private static final int SECOND_KILLER_BONUS = 790_000;
  private static final int HISTORY_CEILING = 700_000;
  private static final int VICTIM_WEIGHT = 8;
  private static final int MAX_PLY = 128;
  private static final int SQUARE_COUNT = 64;
  private static final int COLOR_COUNT = 2;

  private final Move[][] killers = new Move[MAX_PLY][2];
  private final int[][][] history = new int[COLOR_COUNT][SQUARE_COUNT][SQUARE_COUNT];

  List<Move> byPromise(
      PositionView position, List<Move> moves, Optional<Move> rememberedBest, int ply) {
    return moves.stream()
        .sorted(
            Comparator.comparingInt(
                    (Move move) ->
                        rememberedBest.filter(move::equals).isPresent()
                            ? REMEMBERED_BEST_BONUS
                            : promise(position, move, ply))
                .reversed())
        .toList();
  }

  /** Credits quiet {@code move} for refuting a node at {@code ply} searched to {@code depth}. */
  void rememberCutoff(PositionView position, Move move, int depth, int ply) {
    if (isCapture(position, move)) {
      return;
    }
    history[colorIndex(position)][move.from().index()][move.to().index()] += depth * depth;
    if (ply < MAX_PLY && !move.equals(killers[ply][0])) {
      killers[ply][1] = killers[ply][0];
      killers[ply][0] = move;
    }
  }

  private int promise(PositionView position, Move move, int ply) {
    Optional<Piece> victim = victimOf(position, move);
    if (victim.isPresent()) {
      PieceType attacker = position.pieceOn(move.from()).orElseThrow().type();
      return CAPTURE_BASE + rank(victim.get().type()) * VICTIM_WEIGHT - rank(attacker);
    }
    if (move instanceof Move.Promotion promotion) {
      return PROMOTION_BASE + rank(promotion.promoted());
    }
    if (ply < MAX_PLY) {
      if (move.equals(killers[ply][0])) {
        return FIRST_KILLER_BONUS;
      }
      if (move.equals(killers[ply][1])) {
        return SECOND_KILLER_BONUS;
      }
    }
    int credit = history[colorIndex(position)][move.from().index()][move.to().index()];
    return Math.min(credit, HISTORY_CEILING);
  }

  private static int colorIndex(PositionView position) {
    return switch (position.sideToMove()) {
      case WHITE -> 0;
      case BLACK -> 1;
    };
  }

  private static Optional<Piece> victimOf(PositionView position, Move move) {
    if (move instanceof Move.EnPassant) {
      return Optional.of(Piece.of(position.sideToMove().opposite(), PieceType.PAWN));
    }
    return position.pieceOn(move.to());
  }

  private static int rank(PieceType type) {
    return switch (type) {
      case PAWN -> 1;
      case KNIGHT -> 2;
      case BISHOP -> 3;
      case ROOK -> 4;
      case QUEEN -> 5;
      case KING -> 6;
    };
  }

  static boolean isCapture(PositionView position, Move move) {
    return move instanceof Move.EnPassant || position.pieceOn(move.to()).isPresent();
  }
}
