package org.saitama;

import org.saitama.board.Position;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.fen.Fen;
import org.saitama.search.AlphaBetaSearch;
import org.saitama.search.SearchAlgorithm;
import org.saitama.search.SearchResult;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    SearchAlgorithm engine = new AlphaBetaSearch(new ClassicalEvaluator());
    Position start = Fen.parse(Fen.STARTING);
    report("Opening choice at depth 4", engine.search(start, 4));
    Position mateInTwo = Fen.parse("7k/8/8/8/8/8/1R6/R5K1 w - - 0 1");
    report("Mate-in-two puzzle at depth 3", engine.search(mateInTwo, 3));
    Position poisonedPawn = Fen.parse("4k3/8/4p3/3p4/8/8/8/3QK3 w - - 0 1");
    report("Poisoned pawn declined at depth 1", engine.search(poisonedPawn, 1));
  }

  private static void report(String label, SearchResult result) {
    String move =
        result
            .bestMove()
            .map(best -> best.from().algebraic() + best.to().algebraic())
            .orElse("none");
    IO.println(
        label + ": " + move + " (score " + result.score() + ", " + result.nodes() + " nodes)");
  }
}
