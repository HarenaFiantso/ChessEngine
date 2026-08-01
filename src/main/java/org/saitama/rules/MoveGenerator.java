package org.saitama.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Board;
import org.saitama.board.Color;
import org.saitama.board.Direction;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Rank;
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
      case KING -> kingMoves(position, from);
      case BISHOP -> sliderMoves(position, from, Direction.DIAGONAL);
      case ROOK -> sliderMoves(position, from, Direction.ORTHOGONAL);
      case QUEEN -> queenMoves(position, from);
      case PAWN -> pawnMoves(position, from);
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

  private static List<Move> kingMoves(Position position, Square from) {
    List<Move> moves = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      Optional<Square> destination = from.neighbor(direction);
      if (destination.isPresent() && canLandOn(position, destination.get())) {
        moves.add(new Move.Normal(from, destination.get()));
      }
    }
    return moves;
  }

  private static List<Move> sliderMoves(
      Position position, Square from, List<Direction> directions) {
    List<Move> moves = new ArrayList<>();
    for (Direction direction : directions) {
      Optional<Square> step = from.neighbor(direction);
      while (step.isPresent()) {
        Square to = step.get();
        Optional<Piece> occupant = position.board().pieceOn(to);
        if (occupant.isPresent()) {
          if (occupant.get().color() != position.sideToMove()) {
            moves.add(new Move.Normal(from, to));
          }
          break;
        }
        moves.add(new Move.Normal(from, to));
        step = to.neighbor(direction);
      }
    }
    return moves;
  }

  private static List<Move> queenMoves(Position position, Square from) {
    List<Move> moves = sliderMoves(position, from, Direction.ORTHOGONAL);
    moves.addAll(sliderMoves(position, from, Direction.DIAGONAL));
    return moves;
  }

  private static List<Move> pawnMoves(Position position, Square from) {
    List<Move> moves = new ArrayList<>();
    Color mover = position.sideToMove();
    Board board = position.board();
    int forward = mover == Color.WHITE ? 1 : -1;
    Optional<Square> oneAhead = from.translated(0, forward);
    if (oneAhead.isPresent() && board.pieceOn(oneAhead.get()).isEmpty()) {
      addAdvanceOrPromotions(from, oneAhead.get(), moves);
      Rank startRank = mover == Color.WHITE ? Rank.TWO : Rank.SEVEN;
      Optional<Square> twoAhead = from.translated(0, 2 * forward);
      if (from.rank() == startRank
          && twoAhead.isPresent()
          && board.pieceOn(twoAhead.get()).isEmpty()) {
        moves.add(new Move.Normal(from, twoAhead.get()));
      }
    }
    for (int side : new int[] {-1, 1}) {
      Optional<Square> diagonal = from.translated(side, forward);
      if (diagonal.isEmpty()) {
        continue;
      }
      Square to = diagonal.get();
      if (position.enPassantTarget().filter(to::equals).isPresent()) {
        moves.add(new Move.EnPassant(from, to));
      } else if (board.pieceOn(to).filter(piece -> piece.color() != mover).isPresent()) {
        addAdvanceOrPromotions(from, to, moves);
      }
    }
    return moves;
  }

  private static void addAdvanceOrPromotions(Square from, Square to, List<Move> moves) {
    if (to.rank() == Rank.EIGHT || to.rank() == Rank.ONE) {
      for (PieceType promoted :
          List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
        moves.add(new Move.Promotion(from, to, promoted));
      }
    } else {
      moves.add(new Move.Normal(from, to));
    }
  }

  private static boolean canLandOn(Position position, Square destination) {
    Optional<Piece> occupant = position.board().pieceOn(destination);
    return occupant.isEmpty() || occupant.get().color() != position.sideToMove();
  }

  private static boolean leavesOwnKingInCheck(Position position, Move move) {
    return Attacks.isInCheck(position.apply(move).board(), position.sideToMove());
  }
}
