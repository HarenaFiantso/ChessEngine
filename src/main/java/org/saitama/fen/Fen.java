package org.saitama.fen;

import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Board;
import org.saitama.board.File;
import org.saitama.board.Piece;
import org.saitama.board.Rank;
import org.saitama.board.Square;

/**
 * Reads and writes Forsyth-Edwards Notation, the standard one-line text encoding of a chess
 * position.
 *
 * <p>Only the piece placement field is supported so far; side to move, castling rights, en passant
 * target, and the move clocks arrive together with the position type. Parsing is deliberately
 * strict: a rank must describe exactly eight squares and empty-square counts may not be split
 * across adjacent digits, so every accepted string has exactly one meaning.
 */
public final class Fen {

  /** Piece placement field of the standard starting position. */
  public static final String STARTING_PLACEMENT = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

  private Fen() {}

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
