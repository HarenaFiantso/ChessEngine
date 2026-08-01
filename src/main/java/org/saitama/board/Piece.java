package org.saitama.board;

import java.util.EnumMap;
import java.util.Map;

/** Colored piece as it appears on the board, one shared instance per color and type combination. */
public enum Piece {
  WHITE_PAWN(Color.WHITE, PieceType.PAWN),
  WHITE_KNIGHT(Color.WHITE, PieceType.KNIGHT),
  WHITE_BISHOP(Color.WHITE, PieceType.BISHOP),
  WHITE_ROOK(Color.WHITE, PieceType.ROOK),
  WHITE_QUEEN(Color.WHITE, PieceType.QUEEN),
  WHITE_KING(Color.WHITE, PieceType.KING),
  BLACK_PAWN(Color.BLACK, PieceType.PAWN),
  BLACK_KNIGHT(Color.BLACK, PieceType.KNIGHT),
  BLACK_BISHOP(Color.BLACK, PieceType.BISHOP),
  BLACK_ROOK(Color.BLACK, PieceType.ROOK),
  BLACK_QUEEN(Color.BLACK, PieceType.QUEEN),
  BLACK_KING(Color.BLACK, PieceType.KING);

  private static final Piece[] VALUES = values();
  private static final Map<Color, Map<PieceType, Piece>> BY_COLOR_AND_TYPE = index();

  private final Color color;
  private final PieceType type;
  private final char fenSymbol;

  Piece(Color color, PieceType type) {
    this.color = color;
    this.type = type;
    this.fenSymbol = color == Color.WHITE ? type.letter() : Character.toLowerCase(type.letter());
  }

  /** Returns the unique piece with the given color and type. */
  public static Piece of(Color color, PieceType type) {
    return BY_COLOR_AND_TYPE.get(color).get(type);
  }

  /**
   * Returns the piece denoted by the given FEN symbol, uppercase for white and lowercase for black,
   * such as {@code 'P'} or {@code 'k'}.
   *
   * @throws IllegalArgumentException if {@code symbol} does not denote a piece
   */
  public static Piece ofFenSymbol(char symbol) {
    for (Piece piece : VALUES) {
      if (piece.fenSymbol == symbol) {
        return piece;
      }
    }
    throw new IllegalArgumentException("Not a FEN piece symbol: '" + symbol + "'");
  }

  private static Map<Color, Map<PieceType, Piece>> index() {
    Map<Color, Map<PieceType, Piece>> byColorAndType = new EnumMap<>(Color.class);
    for (Color color : Color.values()) {
      byColorAndType.put(color, new EnumMap<>(PieceType.class));
    }
    for (Piece piece : VALUES) {
      byColorAndType.get(piece.color).put(piece.type, piece);
    }
    return byColorAndType;
  }

  /** Returns the side this piece belongs to. */
  public Color color() {
    return color;
  }

  /** Returns the kind of this piece. */
  public PieceType type() {
    return type;
  }

  /** Returns this piece's FEN symbol, uppercase for white and lowercase for black. */
  public char fenSymbol() {
    return fenSymbol;
  }
}
