package org.saitama.board;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Complete state of a chess game at one moment: the piece placement plus everything the rules need
 * that the board alone cannot tell.
 *
 * <p>The five non-board components mirror the state fields of FEN. The en passant target, when
 * present, is the square passed over by the pawn that just advanced two ranks, so it must lie on
 * rank 6 when white is to move and on rank 3 when black is to move. The halfmove clock counts
 * halfmoves since the last capture or pawn advance for the fifty-move rule. The fullmove number
 * starts at one and grows by one after each black move.
 *
 * @param board the piece placement
 * @param sideToMove the side whose turn it is
 * @param castlingRights the castling permissions both sides retain
 * @param enPassantTarget the square a double-pushed pawn passed over, if the last move was one
 * @param halfmoveClock halfmoves since the last capture or pawn advance
 * @param fullmoveNumber one-based move counter, incremented after black moves
 */
public record Position(
    Board board,
    Color sideToMove,
    CastlingRights castlingRights,
    Optional<Square> enPassantTarget,
    int halfmoveClock,
    int fullmoveNumber) {

  /** Validates the component and cross-component invariants described above. */
  public Position {
    Objects.requireNonNull(board, "board");
    Objects.requireNonNull(sideToMove, "sideToMove");
    Objects.requireNonNull(castlingRights, "castlingRights");
    Objects.requireNonNull(enPassantTarget, "enPassantTarget");
    if (halfmoveClock < 0) {
      throw new IllegalArgumentException("Halfmove clock must not be negative: " + halfmoveClock);
    }
    if (fullmoveNumber < 1) {
      throw new IllegalArgumentException("Fullmove number starts at one: " + fullmoveNumber);
    }
    enPassantTarget.ifPresent(target -> requireConsistentEnPassantRank(sideToMove, target));
  }

  /**
   * Returns the position after the side to move plays {@code move}.
   *
   * <p>The move must be mechanically possible: a piece of the side to move stands on the origin and
   * the destination does not hold a friendly piece. Full legality, such as not leaving one's king
   * in check, is not verified here; that discipline arrives with attack detection and move
   * generation.
   *
   * @throws IllegalArgumentException if the move is mechanically impossible in this position
   * @throws UnsupportedOperationException for move kinds whose application is not yet implemented
   */
  public Position apply(Move move) {
    Objects.requireNonNull(move, "move");
    Piece mover =
        board
            .pieceOn(move.from())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("No piece stands on " + move.from().algebraic()));
    if (mover.color() != sideToMove) {
      throw new IllegalArgumentException(
          "The piece on " + move.from().algebraic() + " belongs to the opponent");
    }
    return switch (move) {
      case Move.Normal normal -> applyNormal(mover, normal);
      case Move.Promotion _ ->
          throw new UnsupportedOperationException("Promotion is not applied yet");
      case Move.EnPassant _ ->
          throw new UnsupportedOperationException("En passant is not applied yet");
      case Move.Castling _ ->
          throw new UnsupportedOperationException("Castling is not applied yet");
    };
  }

  private Position applyNormal(Piece mover, Move.Normal move) {
    Optional<Piece> captured = board.pieceOn(move.to());
    if (captured.isPresent() && captured.get().color() == sideToMove) {
      throw new IllegalArgumentException(
          "Cannot capture the friendly piece on " + move.to().algebraic());
    }
    boolean resetsClock = mover.type() == PieceType.PAWN || captured.isPresent();
    return new Position(
        board.withoutPiece(move.from()).withPiece(move.to(), mover),
        sideToMove.opposite(),
        castlingRightsAfterTouching(move.from(), move.to()),
        enPassantTargetAfter(mover, move),
        resetsClock ? 0 : halfmoveClock + 1,
        fullmoveNumberAfter());
  }

  private CastlingRights castlingRightsAfterTouching(Square... squares) {
    CastlingRights updated = castlingRights;
    for (Square square : squares) {
      for (CastlingRight right : rightsAnchoredTo(square)) {
        updated = updated.without(right);
      }
    }
    return updated;
  }

  private static List<CastlingRight> rightsAnchoredTo(Square square) {
    return switch (square) {
      case E1 -> List.of(CastlingRight.WHITE_KINGSIDE, CastlingRight.WHITE_QUEENSIDE);
      case H1 -> List.of(CastlingRight.WHITE_KINGSIDE);
      case A1 -> List.of(CastlingRight.WHITE_QUEENSIDE);
      case E8 -> List.of(CastlingRight.BLACK_KINGSIDE, CastlingRight.BLACK_QUEENSIDE);
      case H8 -> List.of(CastlingRight.BLACK_KINGSIDE);
      case A8 -> List.of(CastlingRight.BLACK_QUEENSIDE);
      default -> List.of();
    };
  }

  private Optional<Square> enPassantTargetAfter(Piece mover, Move.Normal move) {
    if (mover.type() != PieceType.PAWN || move.from().file() != move.to().file()) {
      return Optional.empty();
    }
    int fromRank = move.from().rank().index();
    int toRank = move.to().rank().index();
    if (Math.abs(toRank - fromRank) != 2) {
      return Optional.empty();
    }
    return Optional.of(Square.of(move.from().file(), Rank.of((fromRank + toRank) / 2)));
  }

  private int fullmoveNumberAfter() {
    return sideToMove == Color.BLACK ? fullmoveNumber + 1 : fullmoveNumber;
  }

  private static void requireConsistentEnPassantRank(Color sideToMove, Square target) {
    Rank expected = sideToMove == Color.WHITE ? Rank.SIX : Rank.THREE;
    if (target.rank() != expected) {
      throw new IllegalArgumentException(
          "En passant target "
              + target.algebraic()
              + " cannot follow a double push with "
              + sideToMove
              + " to move");
    }
  }
}
