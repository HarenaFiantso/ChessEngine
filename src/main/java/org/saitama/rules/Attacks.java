package org.saitama.rules;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Color;
import org.saitama.board.Direction;
import org.saitama.board.Piece;
import org.saitama.board.PiecePlacement;
import org.saitama.board.PieceType;
import org.saitama.board.PositionView;
import org.saitama.board.Square;

/**
 * Answers whether squares are attacked, the primitive beneath check detection, castling legality,
 * and legal move generation.
 *
 * <p>The test looks outward from the square in question instead of generating the attacker's moves:
 * project every piece's movement pattern in reverse and see whether the matching enemy piece stands
 * at its end. Pawn, knight, and king patterns probe a bounded handful of squares; sliding patterns
 * walk each ray until the first occupied square. The cost is a small constant, independent of how
 * many moves the attacker actually has.
 */
public final class Attacks {

  private Attacks() {}

  /** Returns whether any piece of {@code attacker} attacks {@code square} on {@code board}. */
  public static boolean isAttacked(PiecePlacement board, Square square, Color attacker) {
    Objects.requireNonNull(board, "board");
    Objects.requireNonNull(square, "square");
    Objects.requireNonNull(attacker, "attacker");
    return attackedByPawn(board, square, attacker)
        || attackedByKnight(board, square, attacker)
        || attackedByKing(board, square, attacker)
        || attackedAlongRays(board, square, attacker, Direction.ORTHOGONAL, PieceType.ROOK)
        || attackedAlongRays(board, square, attacker, Direction.DIAGONAL, PieceType.BISHOP);
  }

  /** Returns whether the side to move's king stands attacked in {@code position}. */
  public static boolean isInCheck(PositionView position) {
    Objects.requireNonNull(position, "position");
    return isInCheck(position, position.sideToMove());
  }

  /**
   * Returns whether {@code side}'s king stands attacked on {@code board}.
   *
   * @throws IllegalStateException if {@code side} has no king on the board
   */
  public static boolean isInCheck(PiecePlacement board, Color side) {
    Objects.requireNonNull(board, "board");
    Objects.requireNonNull(side, "side");
    return isAttacked(board, kingSquare(board, side), side.opposite());
  }

  private static Square kingSquare(PiecePlacement board, Color side) {
    return board
        .kingSquare(side)
        .orElseThrow(() -> new IllegalStateException(side + " has no king on the board"));
  }

  private static boolean attackedByPawn(PiecePlacement board, Square square, Color attacker) {
    int towardAttacker = attacker == Color.WHITE ? -1 : 1;
    Piece pawn = Piece.of(attacker, PieceType.PAWN);
    return holds(board, square.translated(-1, towardAttacker), pawn)
        || holds(board, square.translated(1, towardAttacker), pawn);
  }

  private static boolean attackedByKnight(PiecePlacement board, Square square, Color attacker) {
    Piece knight = Piece.of(attacker, PieceType.KNIGHT);
    for (int[] jump : Steps.KNIGHT_JUMPS) {
      if (holds(board, square.translated(jump[0], jump[1]), knight)) {
        return true;
      }
    }
    return false;
  }

  private static boolean attackedByKing(PiecePlacement board, Square square, Color attacker) {
    Piece king = Piece.of(attacker, PieceType.KING);
    for (Direction direction : Direction.values()) {
      if (holds(board, square.neighbor(direction), king)) {
        return true;
      }
    }
    return false;
  }

  private static boolean attackedAlongRays(
      PiecePlacement board,
      Square square,
      Color attacker,
      List<Direction> directions,
      PieceType slider) {
    for (Direction direction : directions) {
      Optional<Square> step = square.neighbor(direction);
      while (step.isPresent()) {
        Optional<Piece> occupant = board.pieceOn(step.get());
        if (occupant.isPresent()) {
          Piece piece = occupant.get();
          if (piece.color() == attacker
              && (piece.type() == slider || piece.type() == PieceType.QUEEN)) {
            return true;
          }
          break;
        }
        step = step.get().neighbor(direction);
      }
    }
    return false;
  }

  private static boolean holds(PiecePlacement board, Optional<Square> square, Piece piece) {
    return square.flatMap(board::pieceOn).filter(piece::equals).isPresent();
  }
}
