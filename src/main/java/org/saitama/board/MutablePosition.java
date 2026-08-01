package org.saitama.board;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

/**
 * The engine's mutable working state: one position that moves are made on and unmade from, with its
 * Zobrist key maintained incrementally.
 *
 * <p>This is machinery, not a domain value. The immutable {@link Position} remains the model the
 * rest of the system reasons about; this class exists because search and legality filtering visit
 * millions of positions and cannot afford a fresh board per candidate. {@link #make} trusts its
 * caller to supply pseudo-legal moves from the generator and performs no mechanical validation;
 * correctness is guaranteed by tests proving {@code make} equivalent to {@link Position#apply} for
 * every legal move of oracle positions. Instances are not thread-safe.
 */
public final class MutablePosition implements PositionView {

  private static final Square[] SQUARES = Square.values();

  private final Piece[] squares = new Piece[SQUARES.length];
  private int castlingBits;
  private Color sideToMove;
  private Square enPassantTarget;
  private int halfmoveClock;
  private int fullmoveNumber;
  private Square whiteKingSquare;
  private Square blackKingSquare;
  private long zobristKey;

  private MutablePosition() {}

  /** Returns a mutable copy of {@code view}. */
  public static MutablePosition copyOf(PositionView view) {
    Objects.requireNonNull(view, "view");
    MutablePosition position = new MutablePosition();
    for (Square square : SQUARES) {
      Piece piece = view.pieceOn(square).orElse(null);
      position.squares[square.index()] = piece;
      if (piece == Piece.WHITE_KING) {
        position.whiteKingSquare = square;
      } else if (piece == Piece.BLACK_KING) {
        position.blackKingSquare = square;
      }
    }
    for (CastlingRight right : CastlingRight.values()) {
      if (view.castlingAllowed(right)) {
        position.castlingBits |= bitOf(right);
      }
    }
    position.sideToMove = view.sideToMove();
    position.enPassantTarget = view.enPassantTarget().orElse(null);
    position.halfmoveClock = view.halfmoveClock();
    position.fullmoveNumber = view.fullmoveNumber();
    position.zobristKey = Zobrist.of(position);
    return position;
  }

  /** Returns an immutable snapshot of the current state. */
  public Position snapshot() {
    Board.Builder builder = Board.builder();
    for (Square square : SQUARES) {
      Piece piece = squares[square.index()];
      if (piece != null) {
        builder.put(square, piece);
      }
    }
    EnumSet<CastlingRight> rights = EnumSet.noneOf(CastlingRight.class);
    for (CastlingRight right : CastlingRight.values()) {
      if (castlingAllowed(right)) {
        rights.add(right);
      }
    }
    return new Position(
        builder.build(),
        sideToMove,
        new CastlingRights(rights),
        Optional.ofNullable(enPassantTarget),
        halfmoveClock,
        fullmoveNumber);
  }

  /** Returns the incrementally maintained Zobrist key. */
  public long zobristKey() {
    return zobristKey;
  }

  @Override
  public Optional<Piece> pieceOn(Square square) {
    return Optional.ofNullable(squares[square.index()]);
  }

  @Override
  public Optional<Square> kingSquare(Color side) {
    return Optional.ofNullable(side == Color.WHITE ? whiteKingSquare : blackKingSquare);
  }

  @Override
  public Color sideToMove() {
    return sideToMove;
  }

  @Override
  public Optional<Square> enPassantTarget() {
    return Optional.ofNullable(enPassantTarget);
  }

  @Override
  public boolean castlingAllowed(CastlingRight right) {
    return (castlingBits & bitOf(right)) != 0;
  }

  @Override
  public int halfmoveClock() {
    return halfmoveClock;
  }

  @Override
  public int fullmoveNumber() {
    return fullmoveNumber;
  }

  /**
   * What {@link #make} changed and {@link #unmake} needs back.
   *
   * @param capturedPiece the piece removed by the move, or null for quiet moves
   * @param capturedSquare where the captured piece stood, or null
   * @param priorEnPassantTarget the en passant target before the move, or null
   * @param priorCastlingBits the retained rights before the move, one bit per right
   * @param priorHalfmoveClock the clock before the move
   * @param priorZobristKey the key before the move
   */
  public record Undo(
      Piece capturedPiece,
      Square capturedSquare,
      Square priorEnPassantTarget,
      int priorCastlingBits,
      int priorHalfmoveClock,
      long priorZobristKey) {}

  /**
   * Plays {@code move}, which must be pseudo-legal here, and returns what {@link #unmake} needs.
   */
  public Undo make(Move move) {
    final int priorBits = castlingBits;
    final Square priorEnPassant = enPassantTarget;
    final int priorClock = halfmoveClock;
    final long priorKey = zobristKey;
    Piece captured = null;
    Square capturedSquare = null;
    switch (move) {
      case Move.Normal normal -> {
        Piece mover = lift(normal.from());
        if (squares[normal.to().index()] != null) {
          capturedSquare = normal.to();
          captured = lift(capturedSquare);
        }
        place(normal.to(), mover);
        boolean pawnMove = mover.type() == PieceType.PAWN;
        halfmoveClock = pawnMove || captured != null ? 0 : halfmoveClock + 1;
        setEnPassantTarget(doublePushTarget(mover, normal));
        decayRights(normal.from());
        decayRights(normal.to());
      }
      case Move.Promotion promotion -> {
        lift(promotion.from());
        if (squares[promotion.to().index()] != null) {
          capturedSquare = promotion.to();
          captured = lift(capturedSquare);
        }
        place(promotion.to(), Piece.of(sideToMove, promotion.promoted()));
        halfmoveClock = 0;
        setEnPassantTarget(null);
        decayRights(promotion.from());
        decayRights(promotion.to());
      }
      case Move.EnPassant enPassant -> {
        Piece mover = lift(enPassant.from());
        capturedSquare = Square.of(enPassant.to().file(), enPassant.from().rank());
        captured = lift(capturedSquare);
        place(enPassant.to(), mover);
        halfmoveClock = 0;
        setEnPassantTarget(null);
      }
      case Move.Castling castling -> {
        Square rookFrom = rookHome(castling);
        Square rookTo = rookDestination(castling);
        place(castling.to(), lift(castling.from()));
        place(rookTo, lift(rookFrom));
        halfmoveClock = halfmoveClock + 1;
        setEnPassantTarget(null);
        decayRights(castling.from());
        decayRights(rookFrom);
      }
    }
    if (sideToMove == Color.BLACK) {
      fullmoveNumber++;
    }
    sideToMove = sideToMove.opposite();
    zobristKey ^= Zobrist.blackToMoveKey();
    return new Undo(captured, capturedSquare, priorEnPassant, priorBits, priorClock, priorKey);
  }

  /** Reverses {@code move}, which must be the most recent {@link #make} with its {@code undo}. */
  public void unmake(Move move, Undo undo) {
    sideToMove = sideToMove.opposite();
    if (sideToMove == Color.BLACK) {
      fullmoveNumber--;
    }
    switch (move) {
      case Move.Normal normal -> place(normal.from(), lift(normal.to()));
      case Move.Promotion promotion -> {
        lift(promotion.to());
        place(promotion.from(), Piece.of(sideToMove, PieceType.PAWN));
      }
      case Move.EnPassant enPassant -> place(enPassant.from(), lift(enPassant.to()));
      case Move.Castling castling -> {
        place(castling.from(), lift(castling.to()));
        place(rookHome(castling), lift(rookDestination(castling)));
      }
    }
    if (undo.capturedPiece() != null) {
      place(undo.capturedSquare(), undo.capturedPiece());
    }
    castlingBits = undo.priorCastlingBits();
    enPassantTarget = undo.priorEnPassantTarget();
    halfmoveClock = undo.priorHalfmoveClock();
    zobristKey = undo.priorZobristKey();
  }

  private Piece lift(Square square) {
    Piece piece = squares[square.index()];
    squares[square.index()] = null;
    zobristKey ^= Zobrist.pieceKey(piece, square);
    if (piece == Piece.WHITE_KING) {
      whiteKingSquare = null;
    } else if (piece == Piece.BLACK_KING) {
      blackKingSquare = null;
    }
    return piece;
  }

  private void place(Square square, Piece piece) {
    squares[square.index()] = piece;
    zobristKey ^= Zobrist.pieceKey(piece, square);
    if (piece == Piece.WHITE_KING) {
      whiteKingSquare = square;
    } else if (piece == Piece.BLACK_KING) {
      blackKingSquare = square;
    }
  }

  private void setEnPassantTarget(Square target) {
    if (enPassantTarget != null) {
      zobristKey ^= Zobrist.enPassantKey(enPassantTarget.file());
    }
    enPassantTarget = target;
    if (target != null) {
      zobristKey ^= Zobrist.enPassantKey(target.file());
    }
  }

  private void decayRights(Square touched) {
    int removed = castlingBits & anchoredBits(touched);
    if (removed == 0) {
      return;
    }
    for (CastlingRight right : CastlingRight.values()) {
      if ((removed & bitOf(right)) != 0) {
        zobristKey ^= Zobrist.castlingKey(right);
      }
    }
    castlingBits &= ~removed;
  }

  private static int bitOf(CastlingRight right) {
    return switch (right) {
      case WHITE_KINGSIDE -> 1;
      case WHITE_QUEENSIDE -> 1 << 1;
      case BLACK_KINGSIDE -> 1 << 2;
      case BLACK_QUEENSIDE -> 1 << 3;
    };
  }

  private static int anchoredBits(Square square) {
    return switch (square) {
      case E1 -> bitOf(CastlingRight.WHITE_KINGSIDE) | bitOf(CastlingRight.WHITE_QUEENSIDE);
      case H1 -> bitOf(CastlingRight.WHITE_KINGSIDE);
      case A1 -> bitOf(CastlingRight.WHITE_QUEENSIDE);
      case E8 -> bitOf(CastlingRight.BLACK_KINGSIDE) | bitOf(CastlingRight.BLACK_QUEENSIDE);
      case H8 -> bitOf(CastlingRight.BLACK_KINGSIDE);
      case A8 -> bitOf(CastlingRight.BLACK_QUEENSIDE);
      default -> 0;
    };
  }

  private static Square doublePushTarget(Piece mover, Move.Normal move) {
    if (mover.type() != PieceType.PAWN || move.from().file() != move.to().file()) {
      return null;
    }
    int fromRank = move.from().rank().index();
    int toRank = move.to().rank().index();
    if (Math.abs(toRank - fromRank) != 2) {
      return null;
    }
    return Square.of(move.from().file(), Rank.of((fromRank + toRank) / 2));
  }

  private static Square rookHome(Move.Castling castling) {
    File file = castling.to().file() == File.G ? File.H : File.A;
    return Square.of(file, castling.from().rank());
  }

  private static Square rookDestination(Move.Castling castling) {
    File file = castling.to().file() == File.G ? File.F : File.D;
    return Square.of(file, castling.from().rank());
  }
}
