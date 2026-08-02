package org.saitama.search;

import org.saitama.board.Position;

/** Strategy for choosing the best move by exploring the game tree to a fixed depth. */
public interface SearchAlgorithm {

  /**
   * Searches {@code position} to {@code depth} halfmoves and returns the best move found.
   *
   * @throws IllegalArgumentException if {@code depth} is below one
   */
  SearchResult search(Position position, int depth);
}
