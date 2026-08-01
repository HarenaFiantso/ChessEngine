package org.saitama.search;

/** Score constants shared by the search algorithms. */
final class Scores {

  /**
   * Base value of delivering checkmate; the ply at which mate occurs is subtracted so that faster
   * mates score higher.
   */
  static final int MATE = 1_000_000;

  /** Bound beyond any reachable score, used to seed alpha-beta windows. */
  static final int INFINITY = 2_000_000;

  private Scores() {}
}
