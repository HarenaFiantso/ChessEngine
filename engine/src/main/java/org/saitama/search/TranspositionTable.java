package org.saitama.search;

import java.util.Optional;
import org.saitama.board.Move;

/**
 * Remembers search results by Zobrist key so transposed move orders reuse work instead of repeating
 * it.
 *
 * <p>The table is a fixed-size power-of-two array indexed by the key's low bits, with the newest
 * entry always replacing the old occupant of a slot. Because alpha-beta windows leave most nodes
 * knowing only a bound on their true score, every entry records whether its score is exact, a lower
 * bound from a beta cutoff, or an upper bound from a fail-low; consumers must respect that
 * distinction or corrupt the search.
 */
public final class TranspositionTable {

  /** How much a stored score says about the node's true value. */
  public enum NodeType {
    EXACT,
    LOWER_BOUND,
    UPPER_BOUND
  }

  /**
   * One remembered search result.
   *
   * @param key the full Zobrist key, verified on probe because many keys share a slot
   * @param depth the remaining search depth the score was computed with
   * @param score the mover-relative score, mate values stored relative to the node
   * @param type whether {@code score} is exact or a bound
   * @param bestMove the move that produced the score, empty when the node failed low
   */
  public record Entry(long key, int depth, int score, NodeType type, Optional<Move> bestMove) {}

  private final Entry[] entries;

  private TranspositionTable(int capacity) {
    this.entries = new Entry[capacity];
  }

  /** Returns a table holding at least {@code entries} slots, rounded up to a power of two. */
  public static TranspositionTable withCapacity(int entries) {
    if (entries < 1) {
      throw new IllegalArgumentException("Capacity starts at one entry: " + entries);
    }
    int rounded = Integer.highestOneBit(entries);
    return new TranspositionTable(rounded < entries ? rounded * 2 : rounded);
  }

  /** Returns a table that stores nothing, for measuring search without transposition reuse. */
  public static TranspositionTable disabled() {
    return new TranspositionTable(0);
  }

  /** Returns the entry stored for {@code key}, if its slot still holds that exact key. */
  public Optional<Entry> probe(long key) {
    if (entries.length == 0) {
      return Optional.empty();
    }
    Entry entry = entries[slot(key)];
    return entry != null && entry.key() == key ? Optional.of(entry) : Optional.empty();
  }

  /** Stores an entry for {@code key}, replacing whatever occupied its slot. */
  public void store(long key, int depth, int score, NodeType type, Optional<Move> bestMove) {
    if (entries.length == 0) {
      return;
    }
    entries[slot(key)] = new Entry(key, depth, score, type, bestMove);
  }

  private int slot(long key) {
    return (int) (key & (entries.length - 1));
  }
}
