package org.saitama.rules;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Color;
import org.saitama.board.Direction;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.PositionView;
import org.saitama.board.Square;

/**
 * Answers what a capture is worth once every recapture on its square has been traded out.
 *
 * <p>A capture that wins a pawn but loses the queen behind it is not worth a pawn, and search wants
 * to know that without searching. The exchange is played out on a scratch board: each side in turn
 * throws its least valuable remaining attacker at the square, values accumulate, and either side
 * may stand pat instead of recapturing at a loss, which the final backward fold expresses.
 * Attackers are rediscovered from the board after every trade, so a rook behind a rook or a bishop
 * behind a pawn joins the exchange the moment its blocker leaves the ray. A king joins only when
 * the opponent has no reply, since recapturing into an attacked square is not a move.
 *
 * <p>Two deliberate simplifications keep the answer conservative for its callers: a capturing
 * promotion trades as the pawn it starts as, understating it, and pins are ignored, which real
 * engines accept because exchange evaluation guides ordering and pruning rather than legality.
 */
public final class StaticExchange {

  private static final int MAX_EXCHANGE_LENGTH = 32;

  private StaticExchange() {}

  /**
   * Returns the centipawn balance the mover can expect from {@code capture} after best-effort
   * recaptures on its destination, positive when the exchange favors the mover.
   */
  public static int evaluate(PositionView position, Move capture) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(capture, "capture");
    Piece[] board = copyBoard(position);
    Square target = capture.to();
    int[] gain = new int[MAX_EXCHANGE_LENGTH];
    gain[0] = firstVictimValue(capture, board);
    board[target.index()] = board[capture.from().index()];
    board[capture.from().index()] = null;
    Color side = position.sideToMove().opposite();
    int depth = 0;
    while (depth + 1 < MAX_EXCHANGE_LENGTH) {
      Square attackerSquare = leastValuableAttacker(board, target, side);
      if (attackerSquare == null) {
        break;
      }
      Piece attacker = board[attackerSquare.index()];
      board[attackerSquare.index()] = null;
      if (attacker.type() == PieceType.KING
          && leastValuableAttacker(board, target, side.opposite()) != null) {
        board[attackerSquare.index()] = attacker;
        break;
      }
      depth++;
      gain[depth] = value(board[target.index()].type()) - gain[depth - 1];
      board[target.index()] = attacker;
      side = side.opposite();
    }
    while (depth > 0) {
      gain[depth - 1] = -Math.max(-gain[depth - 1], gain[depth]);
      depth--;
    }
    return gain[0];
  }

  private static Piece[] copyBoard(PositionView position) {
    Piece[] board = new Piece[Square.values().length];
    for (Square square : Square.values()) {
      board[square.index()] = position.pieceOn(square).orElse(null);
    }
    return board;
  }

  private static int firstVictimValue(Move capture, Piece[] board) {
    if (capture instanceof Move.EnPassant enPassant) {
      Square victimSquare = Square.of(enPassant.to().file(), enPassant.from().rank());
      board[victimSquare.index()] = null;
      return value(PieceType.PAWN);
    }
    Piece victim = board[capture.to().index()];
    if (victim == null) {
      throw new IllegalArgumentException("No piece to capture on " + capture.to().algebraic());
    }
    return value(victim.type());
  }

  private static Square leastValuableAttacker(Piece[] board, Square target, Color side) {
    int towardAttacker = side == Color.WHITE ? -1 : 1;
    for (int fileStep : new int[] {-1, 1}) {
      Optional<Square> origin = target.translated(fileStep, towardAttacker);
      if (origin.isPresent() && holds(board, origin.get(), Piece.of(side, PieceType.PAWN))) {
        return origin.get();
      }
    }
    for (int[] jump : Steps.KNIGHT_JUMPS) {
      Optional<Square> origin = target.translated(jump[0], jump[1]);
      if (origin.isPresent() && holds(board, origin.get(), Piece.of(side, PieceType.KNIGHT))) {
        return origin.get();
      }
    }
    Square bishopLike = slider(board, target, side, Direction.DIAGONAL, PieceType.BISHOP);
    if (bishopLike != null) {
      return bishopLike;
    }
    Square rookLike = slider(board, target, side, Direction.ORTHOGONAL, PieceType.ROOK);
    if (rookLike != null) {
      return rookLike;
    }
    Square queenOnDiagonal = slider(board, target, side, Direction.DIAGONAL, PieceType.QUEEN);
    if (queenOnDiagonal != null) {
      return queenOnDiagonal;
    }
    Square queenOnFile = slider(board, target, side, Direction.ORTHOGONAL, PieceType.QUEEN);
    if (queenOnFile != null) {
      return queenOnFile;
    }
    for (Direction direction : Direction.values()) {
      Optional<Square> origin = target.neighbor(direction);
      if (origin.isPresent() && holds(board, origin.get(), Piece.of(side, PieceType.KING))) {
        return origin.get();
      }
    }
    return null;
  }

  private static Square slider(
      Piece[] board, Square target, Color side, List<Direction> directions, PieceType wanted) {
    for (Direction direction : directions) {
      Optional<Square> step = target.neighbor(direction);
      while (step.isPresent()) {
        Piece occupant = board[step.get().index()];
        if (occupant != null) {
          if (occupant.color() == side && occupant.type() == wanted) {
            return step.get();
          }
          break;
        }
        step = step.get().neighbor(direction);
      }
    }
    return null;
  }

  private static boolean holds(Piece[] board, Square square, Piece piece) {
    return board[square.index()] == piece;
  }

  private static int value(PieceType type) {
    return switch (type) {
      case PAWN -> 100;
      case KNIGHT -> 320;
      case BISHOP -> 330;
      case ROOK -> 500;
      case QUEEN -> 900;
      case KING -> 20_000;
    };
  }
}
