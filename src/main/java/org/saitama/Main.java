package org.saitama;

import org.saitama.board.Position;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;
import org.saitama.rules.Perft;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Position position = Fen.parse(Fen.STARTING);
    IO.println(position.board());
    IO.println(MoveGenerator.legalMoves(position).size() + " legal moves in the starting position");
    for (int depth = 1; depth <= 4; depth++) {
      IO.println("perft(" + depth + ") = " + Perft.count(position, depth));
    }
  }
}
