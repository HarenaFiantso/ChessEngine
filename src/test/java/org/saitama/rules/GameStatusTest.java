package org.saitama.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.saitama.fen.Fen;

class GameStatusTest {

  @ParameterizedTest
  @CsvSource({
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1, ONGOING",
    "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3, CHECKMATE",
    "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4, CHECKMATE",
    "R5k1/5ppp/8/8/8/8/8/6K1 b - - 1 1, CHECKMATE",
    "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1, STALEMATE",
    "k7/8/1Q6/8/8/8/8/7K b - - 0 1, STALEMATE",
    "4k3/4r3/8/8/8/8/8/4K3 w - - 0 1, ONGOING",
    "4k3/8/8/8/8/8/8/4K2R w - - 100 80, DRAW_BY_FIFTY_MOVE_RULE",
    "4k3/8/8/8/8/8/8/4K2R w - - 99 80, ONGOING",
    "R5k1/5ppp/8/8/8/8/8/6K1 b - - 100 90, CHECKMATE",
    "4k3/8/8/8/8/8/8/4K3 w - - 0 1, DRAW_BY_INSUFFICIENT_MATERIAL",
    "4k3/8/8/8/8/8/8/4KB2 w - - 0 1, DRAW_BY_INSUFFICIENT_MATERIAL",
    "4k3/8/8/8/8/8/4N3/4K3 w - - 0 1, DRAW_BY_INSUFFICIENT_MATERIAL",
    "4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1, DRAW_BY_INSUFFICIENT_MATERIAL",
    "4kb2/8/8/8/8/8/8/3BK3 w - - 0 1, ONGOING",
    "4k3/4n3/8/8/8/8/4N3/4K3 w - - 0 1, ONGOING",
    "4k3/8/8/8/8/8/4N3/4KN2 w - - 0 1, ONGOING",
    "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1, ONGOING",
    "4k3/8/8/8/8/8/8/4K2R w - - 0 1, ONGOING"
  })
  void adjudicatesPositions(String record, GameStatus expected) {
    assertEquals(expected, GameStatus.of(Fen.parse(record)));
  }

  @Test
  void checkmateEndsTheGameDecisively() {
    assertTrue(GameStatus.CHECKMATE.isOver());
    assertFalse(GameStatus.CHECKMATE.isDraw());
  }

  @Test
  void stalemateAndDrawsEndWithoutWinner() {
    assertTrue(GameStatus.STALEMATE.isDraw());
    assertTrue(GameStatus.DRAW_BY_FIFTY_MOVE_RULE.isDraw());
    assertTrue(GameStatus.DRAW_BY_INSUFFICIENT_MATERIAL.isDraw());
    assertFalse(GameStatus.ONGOING.isOver());
    assertFalse(GameStatus.ONGOING.isDraw());
  }

  @Test
  void rejectsNullPositions() {
    assertThrows(NullPointerException.class, () -> GameStatus.of(null));
  }
}
