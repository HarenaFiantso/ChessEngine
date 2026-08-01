package org.saitama;

import java.util.List;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.GameStatus;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Position position = Fen.parse(Fen.STARTING);
    List<Move> foolsMate =
        List.of(
            new Move.Normal(Square.F2, Square.F3),
            new Move.Normal(Square.E7, Square.E5),
            new Move.Normal(Square.G2, Square.G4),
            new Move.Normal(Square.D8, Square.H4));
    for (Move move : foolsMate) {
      position = position.apply(move);
    }
    IO.println(position.board());
    IO.println("After 1. f3 e5 2. g4 Qh4");
    IO.println("Verdict: " + GameStatus.of(position));
  }
}
