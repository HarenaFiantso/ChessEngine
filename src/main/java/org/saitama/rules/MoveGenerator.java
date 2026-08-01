package org.saitama.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.Position;
import org.saitama.board.Square;

/**
 * Generates every legal move for the side to move.
 *
 * <p>Generation happens in two stages. Piece patterns first produce pseudo-legal moves, which
 * respect blockers and captures but ignore whether the mover's own king ends up attacked. A
 * legality filter then applies each candidate and discards the ones after which the mover's king
 * stands in check. The filter needs no knowledge of pins or discovered checks; those fall out of
 * asking the real resulting board.
 */
public final class MoveGenerator {

  private MoveGenerator() {}

  /** Returns every legal move for the side to move in {@code position}. */
  public static List<Move> legalMoves(Position position) {
    Objects.requireNonNull(position, "position");
    List<Move> moves = new ArrayList<>();
    for (Square from : Square.values()) {
      Optional<Piece> occupant = position.board().pieceOn(from);
      if (occupant.isPresent() && occupant.get().color() == position.sideToMove()) {
        moves.addAll(pseudoLegalMoves(position, occupant.get(), from));
      }
    }
    moves.removeIf(move -> leavesOwnKingInCheck(position, move));
    return List.copyOf(moves);
  }

  private static List<Move> pseudoLegalMoves(Position position, Piece piece, Square from) {
    return switch (piece.type()) {
      case KNIGHT -> knightMoves(position, from);
      case PAWN, BISHOP, ROOK, QUEEN, KING -> List.of();
    };
  }

  private static List<Move> knightMoves(Position position, Square from) {
    List<Move> moves = new ArrayList<>();
    for (int[] jump : Steps.KNIGHT_JUMPS) {
      Optional<Square> destination = from.translated(jump[0], jump[1]);
      if (destination.isPresent() && canLandOn(position, destination.get())) {
        moves.add(new Move.Normal(from, destination.get()));
      }
    }
    return moves;
  }

  private static boolean canLandOn(Position position, Square destination) {
    Optional<Piece> occupant = position.board().pieceOn(destination);
    return occupant.isEmpty() || occupant.get().color() != position.sideToMove();
  }

  private static boolean leavesOwnKingInCheck(Position position, Move move) {
    return Attacks.isInCheck(position.apply(move).board(), position.sideToMove());
  }
}
