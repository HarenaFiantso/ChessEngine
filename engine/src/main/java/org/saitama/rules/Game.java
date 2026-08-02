package org.saitama.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Zobrist;

/**
 * A game in progress: the current position plus the history a lone position cannot carry.
 *
 * <p>History is kept as Zobrist keys of every position reached, which is exactly what the threefold
 * repetition rule needs: a claimable draw exists when the current key has occurred three times. Key
 * equality stands in for position equality, the standard engine trade. Instances are immutable;
 * playing a move yields a new game.
 */
public final class Game {

  private static final int REPETITION_LIMIT = 3;

  private final Position position;
  private final List<Long> repetitionKeys;

  private Game(Position position, List<Long> repetitionKeys) {
    this.position = position;
    this.repetitionKeys = repetitionKeys;
  }

  /** Returns a game beginning at {@code position} with empty history. */
  public static Game startingWith(Position position) {
    Objects.requireNonNull(position, "position");
    return new Game(position, List.of(Zobrist.of(position)));
  }

  /** Returns the current position. */
  public Position position() {
    return position;
  }

  /**
   * Returns the game after the side to move plays {@code move}.
   *
   * @throws IllegalArgumentException if {@code move} is not legal in the current position
   */
  public Game play(Move move) {
    Objects.requireNonNull(move, "move");
    if (!MoveGenerator.legalMoves(position).contains(move)) {
      throw new IllegalArgumentException("Not a legal move here: " + move);
    }
    Position next = position.apply(move);
    List<Long> keys = new ArrayList<>(repetitionKeys);
    keys.add(Zobrist.of(next));
    return new Game(next, List.copyOf(keys));
  }

  /**
   * Returns the verdict on the game, including the repetition draw a lone position cannot see.
   * Decisive verdicts take precedence: a move that mates ends the game even if it also repeats.
   */
  public GameStatus status() {
    GameStatus positionVerdict = GameStatus.of(position);
    if (positionVerdict == GameStatus.CHECKMATE || positionVerdict == GameStatus.STALEMATE) {
      return positionVerdict;
    }
    if (isRepeatedThreefold()) {
      return GameStatus.DRAW_BY_REPETITION;
    }
    return positionVerdict;
  }

  private boolean isRepeatedThreefold() {
    long current = repetitionKeys.getLast();
    return repetitionKeys.stream().filter(key -> key == current).count() >= REPETITION_LIMIT;
  }
}
