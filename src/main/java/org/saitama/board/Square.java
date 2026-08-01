package org.saitama.board;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * One of the 64 squares of the board.
 *
 * <p>Squares are declared rank by rank starting from a1, so {@link #index()} follows the
 * little-endian rank-file mapping: a1 is 0, h1 is 7, a8 is 56, and h8 is 63. Bitboards will later
 * use the same number as a bit position, which makes this ordering a long-term contract of the
 * board model.
 */
public enum Square {
  A1,
  B1,
  C1,
  D1,
  E1,
  F1,
  G1,
  H1,
  A2,
  B2,
  C2,
  D2,
  E2,
  F2,
  G2,
  H2,
  A3,
  B3,
  C3,
  D3,
  E3,
  F3,
  G3,
  H3,
  A4,
  B4,
  C4,
  D4,
  E4,
  F4,
  G4,
  H4,
  A5,
  B5,
  C5,
  D5,
  E5,
  F5,
  G5,
  H5,
  A6,
  B6,
  C6,
  D6,
  E6,
  F6,
  G6,
  H6,
  A7,
  B7,
  C7,
  D7,
  E7,
  F7,
  G7,
  H7,
  A8,
  B8,
  C8,
  D8,
  E8,
  F8,
  G8,
  H8;

  private static final int FILE_COUNT = 8;
  private static final int RANK_COUNT = 8;
  private static final Square[] VALUES = values();

  /** Returns the square standing on the given file and rank. */
  public static Square of(File file, Rank rank) {
    return VALUES[rank.index() * FILE_COUNT + file.index()];
  }

  /**
   * Returns the square named in lowercase algebraic notation, such as {@code "e4"}.
   *
   * @throws IllegalArgumentException if {@code name} is not a valid square name
   */
  public static Square ofAlgebraic(String name) {
    Objects.requireNonNull(name, "name");
    if (name.length() == 2) {
      int fileIndex = name.charAt(0) - 'a';
      int rankIndex = name.charAt(1) - '1';
      if (0 <= fileIndex && fileIndex < FILE_COUNT && 0 <= rankIndex && rankIndex < RANK_COUNT) {
        return VALUES[rankIndex * FILE_COUNT + fileIndex];
      }
    }
    throw new IllegalArgumentException("Not an algebraic square name: \"" + name + "\"");
  }

  /** Returns the file this square stands on. */
  public File file() {
    return File.of(index() % FILE_COUNT);
  }

  /** Returns the rank this square stands on. */
  public Rank rank() {
    return Rank.of(index() / FILE_COUNT);
  }

  /** Returns the little-endian rank-file index of this square, 0 for a1 through 63 for h8. */
  @SuppressWarnings("EnumOrdinal")
  public int index() {
    return ordinal();
  }

  /**
   * Returns the square {@code fileDelta} files east and {@code rankDelta} ranks north of this one,
   * or empty if that point lies off the board.
   */
  public Optional<Square> translated(int fileDelta, int rankDelta) {
    int fileIndex = file().index() + fileDelta;
    int rankIndex = rank().index() + rankDelta;
    if (fileIndex < 0 || fileIndex >= FILE_COUNT || rankIndex < 0 || rankIndex >= RANK_COUNT) {
      return Optional.empty();
    }
    return Optional.of(VALUES[rankIndex * FILE_COUNT + fileIndex]);
  }

  /** Returns the neighboring square one step toward {@code direction}, or empty at the edge. */
  public Optional<Square> neighbor(Direction direction) {
    Objects.requireNonNull(direction, "direction");
    return translated(direction.fileDelta(), direction.rankDelta());
  }

  /** Returns the lowercase algebraic name of this square, such as {@code "e4"}. */
  public String algebraic() {
    return name().toLowerCase(Locale.ROOT);
  }
}
