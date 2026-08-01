package org.saitama;

import java.util.List;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.Attacks;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Position position = Fen.parse(Fen.STARTING);
    List<Move> game =
        List.of(
            new Move.Normal(Square.E2, Square.E4),
            new Move.Normal(Square.E7, Square.E5),
            new Move.Normal(Square.D1, Square.H5),
            new Move.Normal(Square.E8, Square.E7),
            new Move.Normal(Square.H5, Square.E5));
    for (Move move : game) {
      position = position.apply(move);
    }
    IO.println(position.board());
    IO.println("After 1. e4 e5 2. Qh5 Ke7 3. Qxe5+");
    IO.println(
        switch (position.sideToMove()) {
          case WHITE -> "White to move";
          case BLACK -> "Black to move";
        });
    if (Attacks.isInCheck(position)) {
      IO.println("Check!");
    }
  }
}
