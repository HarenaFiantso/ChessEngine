package org.saitama.fen;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Board;
import org.saitama.board.CastlingRight;
import org.saitama.board.CastlingRights;
import org.saitama.board.Color;
import org.saitama.board.File;
import org.saitama.board.Piece;
import org.saitama.board.Position;
import org.saitama.board.Rank;
import org.saitama.board.Square;

/**
 * Reads and writes Forsyth-Edwards Notation, the standard one-line text encoding of a chess
 * position.
 *
 * <p>Parsing is deliberately strict, accepting only canonical records: a rank must describe exactly
 * eight squares, empty-square counts may not be split across adjacent digits, castling rights
 * appear in KQkq order or as a single dash, and the move counters carry no leading zeros. Every
 * accepted string therefore has exactly one meaning and exactly one spelling, which is what makes
 * parsing and writing exact inverses of each other.
 */
public final class Fen {

  /** Piece placement field of the standard starting position. */
  public static final String STARTING_PLACEMENT = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

  /** Complete FEN record of the standard starting position. */
  public static final String STARTING = STARTING_PLACEMENT + " w KQkq - 0 1";

  private static final int FIELD_COUNT = 6;

  private Fen() {}

  /**
   * Parses a complete six-field FEN record such as {@value #STARTING}.
   *
   * @throws IllegalArgumentException if {@code record} is not a canonical FEN record or describes
   *     an inconsistent position
   */
  public static Position parse(String record) {
    Objects.requireNonNull(record, "record");
    String[] fields = record.split(" ", -1);
    if (fields.length != FIELD_COUNT) {
      throw new IllegalArgumentException(
          "Expected six space-separated FEN fields: \"" + record + "\"");
    }
    return new Position(
        parsePlacement(fields[0]),
        parseSideToMove(fields[1]),
        parseCastlingRights(fields[2]),
        parseEnPassantTarget(fields[3]),
        parseCounter(fields[4], "halfmove clock"),
        parseCounter(fields[5], "fullmove number"));
  }

  /** Writes the complete six-field FEN record describing {@code position}. */
  public static String write(Position position) {
    Objects.requireNonNull(position, "position");
    StringBuilder text = new StringBuilder(writePlacement(position.board()));
    text.append(' ').append(position.sideToMove() == Color.WHITE ? 'w' : 'b').append(' ');
    writeCastlingRights(text, position.castlingRights());
    text.append(' ').append(position.enPassantTarget().map(Square::algebraic).orElse("-"));
    text.append(' ').append(position.halfmoveClock());
    text.append(' ').append(position.fullmoveNumber());
    return text.toString();
  }

  /**
   * Parses a FEN piece placement field such as {@value #STARTING_PLACEMENT}, listing ranks from 8
   * down to 1 separated by slashes.
   *
   * @throws IllegalArgumentException if {@code placement} is not a well-formed placement field
   */
  public static Board parsePlacement(String placement) {
    Objects.requireNonNull(placement, "placement");
    String[] rankFields = placement.split("/", -1);
    if (rankFields.length != Rank.values().length) {
      throw new IllegalArgumentException(
          "Expected eight ranks separated by slashes: \"" + placement + "\"");
    }
    Board.Builder builder = Board.builder();
    for (Rank rank : Rank.values()) {
      parseRank(builder, rank, rankFields[Rank.EIGHT.index() - rank.index()]);
    }
    return builder.build();
  }

  /** Writes the piece placement field describing {@code board}. */
  public static String writePlacement(Board board) {
    Objects.requireNonNull(board, "board");
    StringBuilder text = new StringBuilder();
    for (int rankIndex = Rank.values().length - 1; rankIndex >= 0; rankIndex--) {
      if (!text.isEmpty()) {
        text.append('/');
      }
      writeRank(text, board, Rank.of(rankIndex));
    }
    return text.toString();
  }

  private static void parseRank(Board.Builder builder, Rank rank, String rankField) {
    int fileCount = File.values().length;
    int fileIndex = 0;
    boolean previousWasCount = false;
    for (int i = 0; i < rankField.length(); i++) {
      char symbol = rankField.charAt(i);
      if ('1' <= symbol && symbol <= '8') {
        if (previousWasCount) {
          throw new IllegalArgumentException(
              "Adjacent empty-square counts in rank \"" + rankField + "\"");
        }
        fileIndex += symbol - '0';
        previousWasCount = true;
      } else {
        if (fileIndex >= fileCount) {
          throw new IllegalArgumentException(
              "Rank \"" + rankField + "\" describes more than eight squares");
        }
        builder.put(Square.of(File.of(fileIndex), rank), Piece.ofFenSymbol(symbol));
        fileIndex++;
        previousWasCount = false;
      }
    }
    if (fileIndex != fileCount) {
      throw new IllegalArgumentException(
          "Rank \"" + rankField + "\" does not describe exactly eight squares");
    }
  }

  private static Color parseSideToMove(String field) {
    return switch (field) {
      case "w" -> Color.WHITE;
      case "b" -> Color.BLACK;
      default -> throw new IllegalArgumentException("Not a side to move: \"" + field + "\"");
    };
  }

  private static CastlingRights parseCastlingRights(String field) {
    if (field.equals("-")) {
      return CastlingRights.none();
    }
    EnumSet<CastlingRight> rights = EnumSet.noneOf(CastlingRight.class);
    int fieldIndex = 0;
    for (CastlingRight right : CastlingRight.values()) {
      if (fieldIndex < field.length() && field.charAt(fieldIndex) == right.symbol()) {
        rights.add(right);
        fieldIndex++;
      }
    }
    if (rights.isEmpty() || fieldIndex != field.length()) {
      throw new IllegalArgumentException(
          "Castling rights must be \"-\" or a subset of \"KQkq\" in that order: \"" + field + "\"");
    }
    return new CastlingRights(rights);
  }

  private static Optional<Square> parseEnPassantTarget(String field) {
    return field.equals("-") ? Optional.empty() : Optional.of(Square.ofAlgebraic(field));
  }

  private static int parseCounter(String field, String description) {
    boolean canonical = !field.isEmpty() && (field.length() == 1 || field.charAt(0) != '0');
    for (int i = 0; canonical && i < field.length(); i++) {
      canonical = '0' <= field.charAt(i) && field.charAt(i) <= '9';
    }
    if (!canonical) {
      throw new IllegalArgumentException(
          "The "
              + description
              + " must be a decimal number without leading zeros: \""
              + field
              + "\"");
    }
    return Integer.parseInt(field);
  }

  private static void writeCastlingRights(StringBuilder text, CastlingRights castlingRights) {
    int lengthBefore = text.length();
    for (CastlingRight right : CastlingRight.values()) {
      if (castlingRights.allows(right)) {
        text.append(right.symbol());
      }
    }
    if (text.length() == lengthBefore) {
      text.append('-');
    }
  }

  private static void writeRank(StringBuilder text, Board board, Rank rank) {
    int emptyRun = 0;
    for (File file : File.values()) {
      Optional<Piece> piece = board.pieceOn(Square.of(file, rank));
      if (piece.isPresent()) {
        if (emptyRun > 0) {
          text.append((char) ('0' + emptyRun));
          emptyRun = 0;
        }
        text.append(piece.get().fenSymbol());
      } else {
        emptyRun++;
      }
    }
    if (emptyRun > 0) {
      text.append((char) ('0' + emptyRun));
    }
  }
}
