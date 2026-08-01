package org.saitama.board;

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
