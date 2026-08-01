package org.saitama.search;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Position;

/**
 * Orders moves so alpha-beta meets its best candidates first: captures by most valuable victim and
 * least valuable attacker, then promotions, then quiet moves.
 *
 * <p>Pruning efficiency depends entirely on encountering a strong move early; every later move is
 * then refuted cheaply. The ranks used here are relative worth only, deliberately independent of
 * the evaluator's centipawn policy.
 */
final class MoveOrdering {

  private static final int REMEMBERED_BEST_BONUS = 1_000_000;
  private static final int CAPTURE_BASE = 1_000;
  private static final int PROMOTION_BASE = 500;
  private static final int VICTIM_WEIGHT = 8;

  private MoveOrdering() {}

  static List<Move> byPromise(Position position, List<Move> moves) {
    return byPromise(position, moves, Optional.empty());
  }

  static List<Move> byPromise(Position position, List<Move> moves, Optional<Move> rememberedBest) {
    return moves.stream()
        .sorted(
            Comparator.comparingInt(
                    (Move move) ->
                        rememberedBest.filter(move::equals).isPresent()
                            ? REMEMBERED_BEST_BONUS
                            : promise(position, move))
                .reversed())
        .toList();
  }

  private static int promise(Position position, Move move) {
    int score = 0;
    Optional<Piece> victim = victimOf(position, move);
    if (victim.isPresent()) {
      PieceType attacker = position.board().pieceOn(move.from()).orElseThrow().type();
      score += CAPTURE_BASE + rank(victim.get().type()) * VICTIM_WEIGHT - rank(attacker);
    }
    if (move instanceof Move.Promotion promotion) {
      score += PROMOTION_BASE + rank(promotion.promoted());
    }
    return score;
  }

  private static Optional<Piece> victimOf(Position position, Move move) {
    if (move instanceof Move.EnPassant) {
      return Optional.of(Piece.of(position.sideToMove().opposite(), PieceType.PAWN));
    }
    return position.board().pieceOn(move.to());
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

  static boolean isCapture(Position position, Move move) {
    return move instanceof Move.EnPassant || position.board().pieceOn(move.to()).isPresent();
  }
}
