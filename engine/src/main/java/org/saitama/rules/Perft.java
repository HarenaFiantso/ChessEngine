package org.saitama.rules;

import java.util.Objects;
import org.saitama.board.Color;
import org.saitama.board.Move;
import org.saitama.board.MutablePosition;
import org.saitama.board.Position;

/**
 * Counts leaf nodes of the legal move tree, the standard correctness audit for chess engines.
 *
 * <p>The number of positions reachable in exactly N halfmoves from well-known starting points is
 * mathematically established. Matching those counts exercises move generation, move application,
 * and the legality filter together; a single wrong edge case, an en passant pin or a castling right
 * surviving a rook capture, shows up as a count that is off by a handful at depth four or five.
 */
public final class Perft {

  private Perft() {}

  /**
   * Returns the number of leaf nodes of the legal move tree of {@code position} at {@code depth}
   * halfmoves.
   *
   * @throws IllegalArgumentException if {@code depth} is negative
   */
  public static long count(Position position, int depth) {
    Objects.requireNonNull(position, "position");
    if (depth < 0) {
      throw new IllegalArgumentException("Depth must not be negative: " + depth);
    }
    return count(MutablePosition.copyOf(position), depth);
  }

  private static long count(MutablePosition position, int depth) {
    if (depth == 0) {
      return 1;
    }
    long nodes = 0;
    Color mover = position.sideToMove();
    for (Move move : MoveGenerator.pseudoLegalMoves(position)) {
      MutablePosition.Undo undo = position.make(move);
      if (!Attacks.isInCheck(position, mover)) {
        nodes += count(position, depth - 1);
      }
      position.unmake(move, undo);
    }
    return nodes;
  }
}
