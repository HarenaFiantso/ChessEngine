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
public final class Board implements PiecePlacement {

  private final Piece[] squares;
  private final Square whiteKingSquare;
  private final Square blackKingSquare;

  private Board(Piece[] squares, Square whiteKingSquare, Square blackKingSquare) {
    this.squares = squares;
    this.whiteKingSquare = whiteKingSquare;
    this.blackKingSquare = blackKingSquare;
  }

  /** Returns a board with no pieces on it. */
  public static Board empty() {
    return new Board(new Piece[Square.values().length], null, null);
  }

  /** Returns a builder that assembles a placement square by square. */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Optional<Piece> pieceOn(Square square) {
    Objects.requireNonNull(square, "square");
    return Optional.ofNullable(squares[square.index()]);
  }

  @Override
  public Optional<Square> kingSquare(Color side) {
    Objects.requireNonNull(side, "side");
    return Optional.ofNullable(side == Color.WHITE ? whiteKingSquare : blackKingSquare);
  }

  /** Returns a board identical to this one except that {@code piece} stands on {@code square}. */
  public Board withPiece(Square square, Piece piece) {
    Objects.requireNonNull(square, "square");
    Objects.requireNonNull(piece, "piece");
    Piece[] updated = squares.clone();
    updated[square.index()] = piece;
    return new Board(
        updated,
        kingAfterPlacement(whiteKingSquare, Piece.WHITE_KING, square, piece),
        kingAfterPlacement(blackKingSquare, Piece.BLACK_KING, square, piece));
  }

  private static Square kingAfterPlacement(Square current, Piece king, Square square, Piece piece) {
    if (piece == king) {
      return square;
    }
    return square == current ? null : current;
  }

  /** Returns a board identical to this one except that {@code square} is vacant. */
  public Board withoutPiece(Square square) {
    Objects.requireNonNull(square, "square");
    if (squares[square.index()] == null) {
      return this;
    }
    Piece[] updated = squares.clone();
    updated[square.index()] = null;
    return new Board(
        updated,
        square == whiteKingSquare ? null : whiteKingSquare,
        square == blackKingSquare ? null : blackKingSquare);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Board board && Arrays.equals(squares, board.squares);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(squares);
  }

  /**
   * Renders the board from white's perspective, rank 8 at the top, using FEN piece symbols and dots
   * for empty squares. Intended for debugging and command-line display, not for parsing.
   */
  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    for (int rankIndex = Rank.values().length - 1; rankIndex >= 0; rankIndex--) {
      Rank rank = Rank.of(rankIndex);
      text.append(rank.digit()).append(' ');
      for (File file : File.values()) {
        Piece piece = squares[Square.of(file, rank).index()];
        text.append(' ').append(piece == null ? '.' : piece.fenSymbol());
      }
      text.append('\n');
    }
    text.append('\n').append(' ').append(' ');
    for (File file : File.values()) {
      text.append(' ').append(file.letter());
    }
    text.append('\n');
    return text.toString();
  }

  /** Assembles a {@link Board} one square at a time. */
  public static final class Builder {

    private final Piece[] squares = new Piece[Square.values().length];
    private Square whiteKingSquare;
    private Square blackKingSquare;

    private Builder() {}

    /** Places {@code piece} on {@code square}, replacing whatever stood there. */
    public Builder put(Square square, Piece piece) {
      Objects.requireNonNull(square, "square");
      Objects.requireNonNull(piece, "piece");
      squares[square.index()] = piece;
      whiteKingSquare = kingAfterPlacement(whiteKingSquare, Piece.WHITE_KING, square, piece);
      blackKingSquare = kingAfterPlacement(blackKingSquare, Piece.BLACK_KING, square, piece);
      return this;
    }

    /** Returns the assembled board, leaving the builder reusable. */
    public Board build() {
      return new Board(squares.clone(), whiteKingSquare, blackKingSquare);
    }
  }
}
