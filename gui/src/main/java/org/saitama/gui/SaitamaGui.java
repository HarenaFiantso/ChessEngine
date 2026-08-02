package org.saitama.gui;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.saitama.board.Color;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.rules.Attacks;
import org.saitama.search.AlphaBetaSearch;
import org.saitama.search.IterativeDeepeningSearch;
import org.saitama.search.SearchLimits;
import org.saitama.search.SearchResult;

/**
 * The JavaFX window: the human plays white by clicking, Saitama answers as black.
 *
 * <p>The window is a thin shell around {@link GameSession}, which owns every interaction rule.
 * Thinking happens on one background thread so the interface never freezes; results return to the
 * JavaFX application thread and are discarded if a new game started while the engine thought. The
 * same deepening search serves the whole session, so its transposition table keeps warming from
 * move to move.
 */
public final class SaitamaGui extends Application {

  private static final Duration ENGINE_BUDGET = Duration.ofMillis(800);

  private final GameSession session = new GameSession();
  private final IterativeDeepeningSearch search =
      new IterativeDeepeningSearch(new AlphaBetaSearch(new ClassicalEvaluator()));
  private final ExecutorService engineExecutor =
      Executors.newSingleThreadExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "saitama-gui-search");
            thread.setDaemon(true);
            return thread;
          });

  private BoardView board;
  private Label statusLine;
  private boolean engineThinking;
  private int gameGeneration;

  /** Launches the interface. */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    board = new BoardView(this::onSquareClicked);
    statusLine = new Label();
    Button newGame = new Button("New game");
    newGame.setOnAction(event -> restart());
    HBox controls = new HBox(16, newGame, statusLine);
    controls.setPadding(new Insets(12));
    controls.setAlignment(Pos.CENTER_LEFT);
    BorderPane root = new BorderPane();
    root.setCenter(board);
    root.setBottom(controls);
    stage.setScene(new Scene(root));
    stage.setTitle("Saitama");
    stage.setResizable(false);
    refresh();
    stage.show();
  }

  @Override
  public void stop() {
    engineExecutor.shutdownNow();
  }

  private void onSquareClicked(Square square) {
    if (engineThinking) {
      return;
    }
    GameSession.Response response = session.click(square);
    if (response == GameSession.Response.PROMOTING) {
      askPromotionChoice();
    }
    refresh();
    answerIfEngineTurn();
  }

  private void askPromotionChoice() {
    ChoiceDialog<PieceType> dialog =
        new ChoiceDialog<>(
            PieceType.QUEEN,
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT));
    dialog.setTitle("Promotion");
    dialog.setHeaderText("Choose the piece the pawn becomes");
    Optional<PieceType> choice = dialog.showAndWait();
    if (choice.isPresent()) {
      session.promote(choice.get());
    } else {
      session.abandonPromotion();
    }
  }

  private void answerIfEngineTurn() {
    if (engineThinking
        || session.status().isOver()
        || session.position().sideToMove() != Color.BLACK) {
      return;
    }
    engineThinking = true;
    int generation = gameGeneration;
    Position position = session.position();
    refresh();
    engineExecutor.execute(
        () -> {
          SearchResult result = search.search(position, SearchLimits.moveTime(ENGINE_BUDGET));
          Platform.runLater(
              () -> {
                if (generation != gameGeneration) {
                  return;
                }
                engineThinking = false;
                result.bestMove().ifPresent(session::play);
                refresh();
              });
        });
  }

  private void restart() {
    gameGeneration++;
    engineThinking = false;
    session.reset();
    refresh();
  }

  private void refresh() {
    board.render(session.position(), session.selectedSquare().orElse(null), session.targets());
    statusLine.setText(statusText());
  }

  private String statusText() {
    if (engineThinking) {
      return "Saitama is thinking";
    }
    return switch (session.status()) {
      case ONGOING -> turnText();
      case CHECKMATE ->
          session.position().sideToMove() == Color.WHITE
              ? "Checkmate. Saitama wins"
              : "Checkmate. You win";
      case STALEMATE -> "Draw by stalemate";
      case DRAW_BY_FIFTY_MOVE_RULE -> "Draw by the fifty move rule";
      case DRAW_BY_INSUFFICIENT_MATERIAL -> "Draw by insufficient material";
      case DRAW_BY_REPETITION -> "Draw by repetition";
    };
  }

  private String turnText() {
    String turn = session.position().sideToMove() == Color.WHITE ? "Your move" : "Saitama to move";
    return Attacks.isInCheck(session.position()) ? turn + ", check" : turn;
  }
}
