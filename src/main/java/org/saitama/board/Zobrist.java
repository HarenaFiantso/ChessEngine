package org.saitama.board;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;

/**
 * Hashes positions into 64-bit keys by the Zobrist scheme: every board feature owns a fixed random
 * number, and a position's key is the exclusive-or of the numbers of its present features.
 *
 * <p>Two positions receive the same key exactly when they agree on piece placement, side to move,
 * castling rights, and en passant file; the move clocks are deliberately excluded because the
 * repetition rule ignores them. Exclusive-or is self-inverse, which is what will later allow
 * incremental key maintenance during make and unmake instead of the full scan performed here.
 * Distinct positions can collide in principle; consumers accept key equality as identity, the
 * standard engine trade.
 */
public final class Zobrist {

  private static final long RANDOM_SEED = 20260801;
  private static final Map<Piece, long[]> PIECE_SQUARE_KEYS = new EnumMap<>(Piece.class);
  private static final Map<CastlingRight, Long> CASTLING_KEYS = new EnumMap<>(CastlingRight.class);
  private static final long[] EN_PASSANT_FILE_KEYS = new long[File.values().length];
  private static final long BLACK_TO_MOVE_KEY;

  static {
    SplittableRandom random = new SplittableRandom(RANDOM_SEED);
    for (Piece piece : Piece.values()) {
      long[] keys = new long[Square.values().length];
      for (int i = 0; i < keys.length; i++) {
        keys[i] = random.nextLong();
      }
      PIECE_SQUARE_KEYS.put(piece, keys);
    }
    for (CastlingRight right : CastlingRight.values()) {
      CASTLING_KEYS.put(right, random.nextLong());
    }
    for (int i = 0; i < EN_PASSANT_FILE_KEYS.length; i++) {
      EN_PASSANT_FILE_KEYS[i] = random.nextLong();
    }
    BLACK_TO_MOVE_KEY = random.nextLong();
  }

  private Zobrist() {}

  /** Returns the Zobrist key of {@code position}. */
  public static long of(Position position) {
    long key = 0;
    for (Square square : Square.values()) {
      Optional<Piece> occupant = position.board().pieceOn(square);
      if (occupant.isPresent()) {
        key ^= PIECE_SQUARE_KEYS.get(occupant.get())[square.index()];
      }
    }
    if (position.sideToMove() == Color.BLACK) {
      key ^= BLACK_TO_MOVE_KEY;
    }
    for (CastlingRight right : CastlingRight.values()) {
      if (position.castlingRights().allows(right)) {
        key ^= CASTLING_KEYS.get(right);
      }
    }
    Optional<Square> target = position.enPassantTarget();
    if (target.isPresent()) {
      key ^= EN_PASSANT_FILE_KEYS[target.get().file().index()];
    }
    return key;
  }
}
