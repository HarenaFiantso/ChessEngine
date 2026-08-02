package org.saitama.board;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable set of castling permissions a position retains.
 *
 * <p>A retained right only records that the king and the relevant rook have never moved. Whether
 * castling is actually playable at a given moment also depends on checks and occupied squares,
 * which is move generation's concern, not state the position carries.
 *
 * @param rights the retained permissions; defensively copied on construction
 */
public record CastlingRights(Set<CastlingRight> rights) {

  /** Canonicalizes the component into an immutable copy. */
  public CastlingRights {
    rights = Set.copyOf(rights);
  }

  /** Returns the rights of the starting position, with all four permissions retained. */
  public static CastlingRights all() {
    return new CastlingRights(Set.of(CastlingRight.values()));
  }

  /** Returns rights with no permission retained. */
  public static CastlingRights none() {
    return new CastlingRights(Set.of());
  }

  /**
   * Returns exactly the given rights.
   *
   * @throws IllegalArgumentException if a right is given more than once
   */
  public static CastlingRights of(CastlingRight... rights) {
    return new CastlingRights(Set.of(rights));
  }

  /** Returns whether the given right is retained. */
  public boolean allows(CastlingRight right) {
    return rights.contains(right);
  }

  /** Returns rights identical to these except that {@code right} is no longer retained. */
  public CastlingRights without(CastlingRight right) {
    Objects.requireNonNull(right, "right");
    if (!allows(right)) {
      return this;
    }
    EnumSet<CastlingRight> remaining = EnumSet.noneOf(CastlingRight.class);
    remaining.addAll(rights);
    remaining.remove(right);
    return new CastlingRights(remaining);
  }
}
