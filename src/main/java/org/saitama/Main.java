package org.saitama;

import org.saitama.board.Position;
import org.saitama.fen.Fen;

/** Command-line entry point of the chess engine. */
public class Main {
  static void main() {
    Position position = Fen.parse(Fen.STARTING);
    IO.println(position.board());
    IO.println(
        switch (position.sideToMove()) {
          case WHITE -> "White to move";
          case BLACK -> "Black to move";
        });
  }
}
