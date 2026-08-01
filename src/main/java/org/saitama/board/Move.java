package org.saitama.board;

import java.util.Objects;

/**
 * One halfmove as its mover states it: origin, destination, and for promotions the piece chosen.
 *
 * <p>The four permitted variants mirror the mechanically distinct kinds of chess moves, so code
 * applying a move can switch over them exhaustively. Each record validates the position-independent
 * geometry of its kind at construction; whether the move is actually playable in a given position
 * is decided where the position is known.
 */
public sealed interface Move {

  /** Returns the square the moving piece leaves. */
  Square from();

  /** Returns the square the moving piece lands on. */
  Square to();

  /** Ordinary move of one piece, quiet or capturing, including pawn double pushes. */
  record Normal(Square from, Square to) implements Move {

    /** Validates that the move leaves its origin. */
    public Normal {
      requireDistinctSquares(from, to);
    }
  }

  /** Pawn stepping onto the last rank and becoming {@code promoted}. */
  record Promotion(Square from, Square to, PieceType promoted) implements Move {

    /** Validates the promotion geometry and the chosen piece. */
    public Promotion {
      requireDistinctSquares(from, to);
      Objects.requireNonNull(promoted, "promoted");
      if (promoted == PieceType.PAWN || promoted == PieceType.KING) {
        throw new IllegalArgumentException("A pawn cannot promote to " + promoted);
      }
      boolean whiteShaped = from.rank() == Rank.SEVEN && to.rank() == Rank.EIGHT;
      boolean blackShaped = from.rank() == Rank.TWO && to.rank() == Rank.ONE;
      if (!(whiteShaped || blackShaped) || fileDistance(from, to) > 1) {
        throw new IllegalArgumentException(
            "Not a promotion journey: " + from.algebraic() + " to " + to.algebraic());
      }
    }
  }

  /** Pawn capturing en passant by landing on the en passant target square. */
  record EnPassant(Square from, Square to) implements Move {

    /** Validates the en passant geometry. */
    public EnPassant {
      requireDistinctSquares(from, to);
      boolean whiteShaped = from.rank() == Rank.FIVE && to.rank() == Rank.SIX;
      boolean blackShaped = from.rank() == Rank.FOUR && to.rank() == Rank.THREE;
      if (!(whiteShaped || blackShaped) || fileDistance(from, to) != 1) {
        throw new IllegalArgumentException(
            "Not an en passant journey: " + from.algebraic() + " to " + to.algebraic());
      }
    }
  }

  /** King castling, stated as the king's two-square journey; the rook follows by rule. */
  record Castling(Square from, Square to) implements Move {

    /** Validates that the journey is one of the four castling king paths. */
    public Castling {
      requireDistinctSquares(from, to);
      boolean white = from == Square.E1 && (to == Square.G1 || to == Square.C1);
      boolean black = from == Square.E8 && (to == Square.G8 || to == Square.C8);
      if (!(white || black)) {
        throw new IllegalArgumentException(
            "Not a castling journey: " + from.algebraic() + " to " + to.algebraic());
      }
    }
  }

  private static void requireDistinctSquares(Square from, Square to) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (from == to) {
      throw new IllegalArgumentException(
          "A move must leave its origin square: " + from.algebraic());
    }
  }

  private static int fileDistance(Square from, Square to) {
    return Math.abs(from.file().index() - to.file().index());
  }
}
