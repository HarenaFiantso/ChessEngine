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
 *
 * <p>Deepening also funds a second bet: each iteration knows the previous score, so instead of
 * searching with an unbounded window it aspires to a narrow one around that score, which prunes
 * harder everywhere. A score landing on or outside the window's edge is only a bound, so the
 * iteration re-searches with the full window; the gamble costs a re-search when scores swing, and
 * pays everywhere they do not. Mate scores are never aspired around, because their arithmetic is
 * distance, not evaluation.
 */
public final class IterativeDeepeningSearch implements SearchAlgorithm {

  private static final int UNLIMITED_DEPTH = 64;

  private static final int ASPIRATION_MARGIN = 50;

  private final AlphaBetaSearch delegate;
  private final LongSupplier nanoTime;
  private final boolean aspirationWindows;

  /** Creates a deepening search over {@code delegate}, timing itself with the system clock. */
  public IterativeDeepeningSearch(AlphaBetaSearch delegate) {
    this(delegate, System::nanoTime);
  }

  /** Creates a deepening search over {@code delegate}, timing itself with {@code nanoTime}. */
  public IterativeDeepeningSearch(AlphaBetaSearch delegate, LongSupplier nanoTime) {
    this(delegate, nanoTime, true);
  }

  /**
   * Creates a deepening search with aspiration windows switchable, which only measurement and
   * comparison tests need.
   */
  IterativeDeepeningSearch(
      AlphaBetaSearch delegate, LongSupplier nanoTime, boolean aspirationWindows) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    this.aspirationWindows = aspirationWindows;
  }

  @Override
  public SearchResult search(Position position, int depth) {
    return search(position, SearchLimits.depth(depth));
  }

  /** Searches {@code position} until {@code limits} call time. */
  public SearchResult search(Position position, SearchLimits limits) {
    return search(position, limits, IterativeDeepeningSearch::neverStop);
  }

  /**
   * Searches {@code position} until {@code limits} call time or {@code stopSignal} fires.
   *
   * <p>The signal is how another thread ends a search that has no bound of its own, which is the
   * UCI {@code stop} command. Depth one still always completes, so a firing signal never costs the
   * guaranteed legal move.
   */
  public SearchResult search(Position position, SearchLimits limits, BooleanSupplier stopSignal) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(limits, "limits");
    Objects.requireNonNull(stopSignal, "stopSignal");
    long startNanos = nanoTime.getAsLong();
    BooleanSupplier expired =
        limits
            .maxMoveTime()
            .<BooleanSupplier>map(
                budget -> () -> nanoTime.getAsLong() - startNanos >= budget.toNanos())
            .orElse(IterativeDeepeningSearch::neverStop);
    BooleanSupplier halted = () -> stopSignal.getAsBoolean() || expired.getAsBoolean();
    int deepest = limits.maxDepth().orElse(UNLIMITED_DEPTH);
    SearchResult best = delegate.search(position, 1);
    long totalNodes = best.nodes();
    for (int depth = 2; depth <= deepest; depth++) {
      if (Math.abs(best.score()) > Scores.MATE_THRESHOLD || halted.getAsBoolean()) {
        break;
      }
      try {
        SearchResult result = searchAtDepth(position, depth, halted, best.score());
        totalNodes += result.nodes();
        best = new SearchResult(result.bestMove(), result.score(), totalNodes, depth);
      } catch (SearchAborted aborted) {
        break;
      }
    }
    return new SearchResult(best.bestMove(), best.score(), totalNodes, best.depth());
  }

  private SearchResult searchAtDepth(
      Position position, int depth, BooleanSupplier halted, int previousScore) {
    if (!aspirationWindows || Math.abs(previousScore) > Scores.MATE_THRESHOLD) {
      return delegate.search(position, depth, halted);
    }
    int alpha = previousScore - ASPIRATION_MARGIN;
    int beta = previousScore + ASPIRATION_MARGIN;
    SearchResult guess = delegate.search(position, depth, halted, alpha, beta);
    if (guess.score() > alpha && guess.score() < beta) {
      return guess;
    }
    SearchResult certain = delegate.search(position, depth, halted);
    return new SearchResult(
        certain.bestMove(), certain.score(), guess.nodes() + certain.nodes(), certain.depth());
  }

  private static boolean neverStop() {
    return false;
  }
}
