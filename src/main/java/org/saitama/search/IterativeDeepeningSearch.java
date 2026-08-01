package org.saitama.search;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import org.saitama.board.Position;

/**
 * Searches depth one, then two, then deeper, until a limit calls time, and answers from the deepest
 * fully completed iteration.
 *
 * <p>Repeating shallow searches costs less than it seems: game trees grow exponentially, so all
 * previous iterations together cost a fraction of the final one, and each iteration fills the
 * transposition table whose remembered best moves make the next iteration far cheaper than a cold
 * start. In exchange the search becomes an anytime algorithm: a fully searched move is always in
 * hand when the clock fires, which is what real play requires. Depth one always completes
 * regardless of budget so a legal move is guaranteed.
 */
public final class IterativeDeepeningSearch implements SearchAlgorithm {

  private static final int UNLIMITED_DEPTH = 64;

  private final AlphaBetaSearch delegate;
  private final LongSupplier nanoTime;

  /** Creates a deepening search over {@code delegate}, timing itself with the system clock. */
  public IterativeDeepeningSearch(AlphaBetaSearch delegate) {
    this(delegate, System::nanoTime);
  }

  /** Creates a deepening search over {@code delegate}, timing itself with {@code nanoTime}. */
  public IterativeDeepeningSearch(AlphaBetaSearch delegate, LongSupplier nanoTime) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
  }

  @Override
  public SearchResult search(Position position, int depth) {
    return search(position, SearchLimits.depth(depth));
  }

  /** Searches {@code position} until {@code limits} call time. */
  public SearchResult search(Position position, SearchLimits limits) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(limits, "limits");
    long startNanos = nanoTime.getAsLong();
    BooleanSupplier expired =
        limits
            .maxMoveTime()
            .<BooleanSupplier>map(
                budget -> () -> nanoTime.getAsLong() - startNanos >= budget.toNanos())
            .orElse(IterativeDeepeningSearch::neverStop);
    int deepest = limits.maxDepth().orElse(UNLIMITED_DEPTH);
    SearchResult best = delegate.search(position, 1);
    long totalNodes = best.nodes();
    for (int depth = 2; depth <= deepest; depth++) {
      if (Math.abs(best.score()) > Scores.MATE_THRESHOLD || expired.getAsBoolean()) {
        break;
      }
      try {
        SearchResult result = delegate.search(position, depth, expired);
        totalNodes += result.nodes();
        best = new SearchResult(result.bestMove(), result.score(), totalNodes, depth);
      } catch (SearchAborted aborted) {
        break;
      }
    }
    return new SearchResult(best.bestMove(), best.score(), totalNodes, best.depth());
  }

  private static boolean neverStop() {
    return false;
  }
}
