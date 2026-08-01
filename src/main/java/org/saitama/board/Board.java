package org.saitama.board;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable arrangement of pieces on the 64 squares.
 *
 * <p>A board answers the square-centric question of which piece stands where, and nothing more:
 * side to move, castling rights, and the rest of the game state belong to the forthcoming position
 * type. Instances never change after construction and are therefore freely shareable.
 */
public final class Board {

  private final Piece[] squares;

  private Board(Piece[] squares) {
    this.squares = squares;
  }

  /** Returns a board with no pieces on it. */
  public static Board empty() {
    return new Board(new Piece[Square.values().length]);
  }

  /** Returns a builder that assembles a placement square by square. */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns the piece standing on {@code square}, or empty if the square is vacant. */
  public Optional<Piece> pieceOn(Square square) {
    Objects.requireNonNull(square, "square");
    return Optional.ofNullable(squares[square.index()]);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Board board && Arrays.equals(squares, board.squares);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(squares);
  }

  /** Assembles a {@link Board} one square at a time. */
  public static final class Builder {

    private final Piece[] squares = new Piece[Square.values().length];

    private Builder() {}

    /** Places {@code piece} on {@code square}, replacing whatever stood there. */
    public Builder put(Square square, Piece piece) {
      Objects.requireNonNull(square, "square");
      Objects.requireNonNull(piece, "piece");
      squares[square.index()] = piece;
      return this;
    }

    /** Returns the assembled board, leaving the builder reusable. */
    public Board build() {
      return new Board(squares.clone());
    }
  }
}
