package org.saitama;

import java.time.Duration;
import java.util.List;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;
import org.saitama.rules.Game;
import org.saitama.search.AlphaBetaSearch;
import org.saitama.search.IterativeDeepeningSearch;
import org.saitama.search.SearchAlgorithm;
import org.saitama.search.SearchLimits;
import org.saitama.search.SearchResult;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    SearchAlgorithm engine = new AlphaBetaSearch(new ClassicalEvaluator());
    IterativeDeepeningSearch deepening =
        new IterativeDeepeningSearch(new AlphaBetaSearch(new ClassicalEvaluator()));
    Position start = Fen.parse(Fen.STARTING);
    report(
        "Opening choice after one second",
        deepening.search(start, SearchLimits.moveTime(Duration.ofSeconds(1))));
    Position mateInTwo = Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1");
    report("Mate-in-two puzzle at depth 3", engine.search(mateInTwo, 3));
    Position poisonedPawn = Fen.parse("4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1");
    report("Poisoned pawn declined at depth 1", engine.search(poisonedPawn, 1));
    Game shuffle = Game.startingWith(start);
    List<Move> knightShuffle =
        List.of(
            new Move.Normal(Square.G1, Square.F3),
            new Move.Normal(Square.G8, Square.F6),
            new Move.Normal(Square.F3, Square.G1),
            new Move.Normal(Square.F6, Square.G8));
    for (int round = 0; round < 2; round++) {
      for (Move move : knightShuffle) {
        shuffle = shuffle.play(move);
      }
    }
    IO.println("Knight-shuffle verdict after eight plies: " + shuffle.status());
  }

  private static void report(String label, SearchResult result) {
    String move =
        result
            .bestMove()
            .map(best -> best.from().algebraic() + best.to().algebraic())
            .orElse("none");
    IO.println(
        label
            + ": "
            + move
            + " (depth "
            + result.depth()
            + ", score "
            + result.score()
            + ", "
            + result.nodes()
            + " nodes)");
  }
}
