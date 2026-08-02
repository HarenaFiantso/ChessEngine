package org.saitama.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.saitama.board.Color;
import org.saitama.board.Position;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;
import org.saitama.rules.Game;
import org.saitama.search.AlphaBetaSearch;
import org.saitama.search.IterativeDeepeningSearch;
import org.saitama.search.SearchLimits;
import org.saitama.search.SearchResult;

/**
 * Speaks the Universal Chess Interface over a line-based reader and writer, which is how every
 * chess GUI drives an engine: the GUI owns the game and the board, the engine owns the thinking,
 * and they exchange plain text.
 *
 * <p>The engine answers {@code uci}, {@code isready}, {@code ucinewgame}, {@code position}, {@code
 * go}, {@code stop}, and {@code quit}, and ignores everything else as the protocol requires.
 * Searching runs on its own worker thread, so the command loop stays responsive while the engine
 * thinks: {@code isready} answers immediately, {@code go infinite} searches until {@code stop}
 * arrives, and {@code stop} or {@code quit} ends the search and waits for its {@code bestmove}. One
 * search runs at a time; a {@code go} during a search is rejected with an info string. The search
 * reads immutable snapshots taken when it starts, so later commands cannot corrupt it.
 */
public final class UciEngine {

  private static final String ENGINE_NAME = "Saitama 0.1";
  private static final String ENGINE_AUTHOR = "Fiantso Harena";
  private static final String NULL_MOVE = "0000";
  private static final Duration DEFAULT_BUDGET = Duration.ofSeconds(2);
  private static final int PLIES_PER_MOVE = 2;

  private final BufferedReader input;
  private final Consumer<String> output;
  private final AtomicBoolean stopRequested = new AtomicBoolean();
  private IterativeDeepeningSearch search = freshSearch();
  private Game game = Game.startingWith(Fen.parse(Fen.STARTING));
  private Thread searchWorker;

  /** Creates an engine reading commands from {@code input} and answering through {@code output}. */
  public UciEngine(Reader input, Consumer<String> output) {
    this.input = new BufferedReader(Objects.requireNonNull(input, "input"));
    this.output = Objects.requireNonNull(output, "output");
  }

  /** Serves the protocol until {@code quit} or the end of input. */
  public void run() throws IOException {
    for (String line = input.readLine(); line != null; line = input.readLine()) {
      if (!handle(line.strip())) {
        break;
      }
    }
    haltSearch();
  }

  private boolean handle(String line) {
    if (line.isEmpty()) {
      return true;
    }
    List<String> tokens = List.of(line.split("\\s+"));
    try {
      switch (tokens.getFirst()) {
        case "uci" -> identify();
        case "isready" -> output.accept("readyok");
        case "ucinewgame" -> resetForNewGame();
        case "position" -> handlePosition(tokens);
        case "go" -> handleGo(tokens);
        case "stop" -> haltSearch();
        case "quit" -> {
          return false;
        }
        default -> {}
      }
    } catch (IllegalArgumentException | IllegalStateException rejected) {
      output.accept("info string rejected: " + rejected.getMessage());
    }
    return true;
  }

  private void identify() {
    output.accept("id name " + ENGINE_NAME);
    output.accept("id author " + ENGINE_AUTHOR);
    output.accept("uciok");
  }

  private void resetForNewGame() {
    search = freshSearch();
    game = Game.startingWith(Fen.parse(Fen.STARTING));
  }

  private void handlePosition(List<String> tokens) {
    int movesIndex = tokens.indexOf("moves");
    int placementEnd = movesIndex < 0 ? tokens.size() : movesIndex;
    Position base =
        switch (tokens.get(1)) {
          case "startpos" -> Fen.parse(Fen.STARTING);
          case "fen" -> Fen.parse(String.join(" ", tokens.subList(2, placementEnd)));
          default ->
              throw new IllegalArgumentException("Expected startpos or fen: " + tokens.get(1));
        };
    Game updated = Game.startingWith(base);
    if (movesIndex >= 0) {
      for (String moveText : tokens.subList(movesIndex + 1, tokens.size())) {
        updated = updated.play(UciMoves.parse(moveText, updated.position()));
      }
    }
    game = updated;
  }

  private void handleGo(List<String> tokens) {
    if (searchWorker != null && searchWorker.isAlive()) {
      throw new IllegalStateException("a search is already running; send stop first");
    }
    SearchLimits limits = limitsFrom(tokens);
    Position position = game.position();
    IterativeDeepeningSearch active = search;
    stopRequested.set(false);
    searchWorker =
        new Thread(
            () -> announce(active.search(position, limits, stopRequested::get)), "saitama-search");
    searchWorker.start();
  }

  private void announce(SearchResult result) {
    output.accept(
        "info depth "
            + result.depth()
            + " score "
            + scoreText(result)
            + " nodes "
            + result.nodes());
    output.accept("bestmove " + result.bestMove().map(UciMoves::format).orElse(NULL_MOVE));
  }

  private void haltSearch() {
    stopRequested.set(true);
    if (searchWorker == null) {
      return;
    }
    try {
      searchWorker.join();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
    searchWorker = null;
  }

  private SearchLimits limitsFrom(List<String> tokens) {
    if (tokens.contains("infinite")) {
      return SearchLimits.unlimited();
    }
    Map<String, Long> numbers = new HashMap<>();
    for (int i = 1; i + 1 < tokens.size(); i++) {
      String value = tokens.get(i + 1);
      if (value.chars().allMatch(Character::isDigit)) {
        numbers.put(tokens.get(i), Long.parseLong(value));
      }
    }
    OptionalInt depth =
        numbers.containsKey("depth")
            ? OptionalInt.of(Math.toIntExact(numbers.get("depth")))
            : OptionalInt.empty();
    Duration budget = budgetFrom(numbers);
    if (depth.isPresent() && budget == null) {
      return SearchLimits.depth(depth.getAsInt());
    }
    Duration effective = budget == null ? DEFAULT_BUDGET : budget;
    return depth.isPresent()
        ? SearchLimits.of(depth.getAsInt(), effective)
        : SearchLimits.moveTime(effective);
  }

  private Duration budgetFrom(Map<String, Long> numbers) {
    if (numbers.containsKey("movetime")) {
      return Duration.ofMillis(numbers.get("movetime"));
    }
    String clock = game.position().sideToMove() == Color.WHITE ? "wtime" : "btime";
    String increment = game.position().sideToMove() == Color.WHITE ? "winc" : "binc";
    if (!numbers.containsKey(clock)) {
      return null;
    }
    OptionalInt movesToGo =
        numbers.containsKey("movestogo")
            ? OptionalInt.of(Math.toIntExact(numbers.get("movestogo")))
            : OptionalInt.empty();
    return TimeAllocation.budgetFor(
        Duration.ofMillis(numbers.get(clock)),
        Duration.ofMillis(numbers.getOrDefault(increment, 0L)),
        movesToGo);
  }

  private static String scoreText(SearchResult result) {
    OptionalInt mate = result.mateDistance();
    if (mate.isPresent()) {
      int plies = mate.getAsInt();
      int moves = plies >= 0 ? (plies + 1) / PLIES_PER_MOVE : -((-plies + 1) / PLIES_PER_MOVE);
      return "mate " + moves;
    }
    return "cp " + result.score();
  }

  private static IterativeDeepeningSearch freshSearch() {
    return new IterativeDeepeningSearch(new AlphaBetaSearch(new ClassicalEvaluator()));
  }
}
