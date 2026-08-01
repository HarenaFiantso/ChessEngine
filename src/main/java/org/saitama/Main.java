package org.saitama;

import java.util.List;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Position position = Fen.parse(Fen.STARTING);
    List<Move> opening =
        List.of(
            new Move.Normal(Square.E2, Square.E4),
            new Move.Normal(Square.C7, Square.C5),
            new Move.Normal(Square.G1, Square.F3));
    for (Move move : opening) {
      position = position.apply(move);
    }
    IO.println(position.board());
    IO.println("After 1. e4 c5 2. Nf3");
    IO.println(Fen.write(position));
  }
}
