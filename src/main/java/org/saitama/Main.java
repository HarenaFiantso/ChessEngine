package org.saitama;

import org.saitama.board.Board;
import org.saitama.fen.Fen;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Board startingPosition = Fen.parsePlacement(Fen.STARTING_PLACEMENT);
    IO.println(startingPosition);
  }
}
