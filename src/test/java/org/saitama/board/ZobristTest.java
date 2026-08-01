package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.saitama.fen.Fen;

class ZobristTest {

  @Test
  void equalPositionsHashEqually() {
    assertEquals(Zobrist.of(Fen.parse(Fen.STARTING)), Zobrist.of(Fen.parse(Fen.STARTING)));
  }

  @Test
  void everyFeatureContributesToTheKey() {
    long reference = Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4K2R w K - 0 1"));
    assertNotEquals(reference, Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4KR2 w - - 0 1")));
    assertNotEquals(reference, Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4K2R b K - 0 1")));
    assertNotEquals(reference, Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4K2R w - - 0 1")));
    assertNotEquals(
        Zobrist.of(Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")),
        Zobrist.of(Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")));
  }

  @Test
  void clocksDoNotContributeToTheKey() {
    assertEquals(
        Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4K3 w - - 0 1")),
        Zobrist.of(Fen.parse("4k3/8/8/8/8/8/8/4K3 w - - 42 90")));
  }

  @Test
  void transposedMoveOrdersReachTheSameKey() {
    Position shuffled = Fen.parse(Fen.STARTING);
    for (Move move :
        List.of(
            new Move.Normal(Square.G1, Square.F3),
            new Move.Normal(Square.G8, Square.F6),
            new Move.Normal(Square.F3, Square.G1),
            new Move.Normal(Square.F6, Square.G8))) {
      shuffled = shuffled.apply(move);
    }
    assertNotEquals(Fen.write(Fen.parse(Fen.STARTING)), Fen.write(shuffled));
    assertEquals(Zobrist.of(Fen.parse(Fen.STARTING)), Zobrist.of(shuffled));
  }
}
