package org.saitama.uci;

import java.util.Objects;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;

/**
 * Reads and writes moves in UCI coordinate notation: origin and destination squares, plus a
 * lowercase piece letter for promotions, as in {@code e2e4} or {@code e7e8q}.
 *
 * <p>The notation names only squares, so parsing needs the position to recover the move kind: a
 * king travelling two files is castling, a pawn stepping diagonally onto the en passant target is
 * an en passant capture, everything else is a normal move.
 */
public final class UciMoves {

  private static final int PLAIN_LENGTH = 4;
  private static final int PROMOTION_LENGTH = 5;

  private UciMoves() {}

  /** Returns {@code move} in coordinate notation. */
  public static String format(Move move) {
    Objects.requireNonNull(move, "move");
    String text = move.from().algebraic() + move.to().algebraic();
    if (move instanceof Move.Promotion promotion) {
      return text + Character.toLowerCase(promotion.promoted().letter());
    }
    return text;
  }

  /**
   * Returns the move written as {@code text}, using {@code position} to recover its kind.
   *
   * @throws IllegalArgumentException if {@code text} is not coordinate notation or names an
   *     impossible move
   */
  public static Move parse(String text, Position position) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(position, "position");
    if (text.length() != PLAIN_LENGTH && text.length() != PROMOTION_LENGTH) {
      throw new IllegalArgumentException("Not coordinate notation: \"" + text + "\"");
    }
    Square from = Square.ofAlgebraic(text.substring(0, 2));
    Square to = Square.ofAlgebraic(text.substring(2, PLAIN_LENGTH));
    if (text.length() == PROMOTION_LENGTH) {
      return new Move.Promotion(from, to, promotedFor(text.charAt(PLAIN_LENGTH), text));
    }
    Piece mover =
        position
            .board()
            .pieceOn(from)
            .orElseThrow(
                () -> new IllegalArgumentException("No piece stands on " + from.algebraic()));
    int fileDistance = Math.abs(from.file().index() - to.file().index());
    if (mover.type() == PieceType.KING && fileDistance == 2) {
      return new Move.Castling(from, to);
    }
    if (mover.type() == PieceType.PAWN
        && fileDistance == 1
        && position.enPassantTarget().filter(to::equals).isPresent()) {
      return new Move.EnPassant(from, to);
    }
    return new Move.Normal(from, to);
  }

  private static PieceType promotedFor(char letter, String text) {
    return switch (letter) {
      case 'q' -> PieceType.QUEEN;
      case 'r' -> PieceType.ROOK;
      case 'b' -> PieceType.BISHOP;
      case 'n' -> PieceType.KNIGHT;
      default -> throw new IllegalArgumentException("Not a promotion piece in \"" + text + "\"");
    };
  }
}
