package org.saitama.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Board;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;

/**
 * Verdict on a position: still being played, decided, or drawn.
 *
 * <p>With legal move generation available, the decisive verdicts are nearly definitions: no legal
 * moves while in check is checkmate, no legal moves otherwise is stalemate. The drawn verdicts
 * cover the fifty-move rule, adjudicated once the halfmove clock reaches one hundred with checkmate
 * taking precedence on the very move that reaches it, and the standard insufficient-material set:
 * bare kings, king and one minor piece, or bishops only that all stand on one shade. Threefold
 * repetition is deliberately absent; a single position cannot know its history, so repetition
 * arrives with the game container.
 */
public enum GameStatus {
  ONGOING,
  CHECKMATE,
  STALEMATE,
  DRAW_BY_FIFTY_MOVE_RULE,
  DRAW_BY_INSUFFICIENT_MATERIAL,
  DRAW_BY_REPETITION;

  private static final int FIFTY_MOVE_HALFMOVE_LIMIT = 100;

  /** Returns the verdict on {@code position}; checkmate loses for the side to move. */
  public static GameStatus of(Position position) {
    Objects.requireNonNull(position, "position");
    if (MoveGenerator.legalMoves(position).isEmpty()) {
      return Attacks.isInCheck(position) ? CHECKMATE : STALEMATE;
    }
    if (position.halfmoveClock() >= FIFTY_MOVE_HALFMOVE_LIMIT) {
      return DRAW_BY_FIFTY_MOVE_RULE;
    }
    if (isInsufficientMaterial(position.board())) {
      return DRAW_BY_INSUFFICIENT_MATERIAL;
    }
    return ONGOING;
  }

  /** Returns whether the game has ended. */
  public boolean isOver() {
    return this != ONGOING;
  }

  /** Returns whether the game has ended without a winner. */
  public boolean isDraw() {
    return isOver() && this != CHECKMATE;
  }

  private static boolean isInsufficientMaterial(Board board) {
    int knightCount = 0;
    List<Boolean> bishopShades = new ArrayList<>();
    for (Square square : Square.values()) {
      Optional<Piece> occupant = board.pieceOn(square);
      if (occupant.isEmpty() || occupant.get().type() == PieceType.KING) {
        continue;
      }
      PieceType type = occupant.get().type();
      if (type != PieceType.BISHOP && type != PieceType.KNIGHT) {
        return false;
      }
      if (type == PieceType.KNIGHT) {
        knightCount++;
      } else {
        bishopShades.add(square.isLight());
      }
    }
    if (knightCount + bishopShades.size() <= 1) {
      return true;
    }
    return knightCount == 0 && bishopShades.stream().distinct().count() == 1;
  }
}
