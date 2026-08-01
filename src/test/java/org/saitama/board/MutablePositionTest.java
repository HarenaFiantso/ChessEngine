package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;

class MutablePositionTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R b KQkq - 0 1",
        "rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 2",
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"
      })
  void makeMatchesApplyAndUnmakeRestoresForEveryLegalMove(String record) {
    Position position = Fen.parse(record);
    long originalKey = Zobrist.of(position);
    for (Move move : MoveGenerator.legalMoves(position)) {
      MutablePosition mutable = MutablePosition.copyOf(position);
      MutablePosition.Undo undo = mutable.make(move);
      assertEquals(position.apply(move), mutable.snapshot(), move.toString());
      assertEquals(Zobrist.of(mutable.snapshot()), mutable.zobristKey(), move.toString());
      mutable.unmake(move, undo);
      assertEquals(position, mutable.snapshot(), move.toString());
      assertEquals(originalKey, mutable.zobristKey(), move.toString());
    }
  }

  @Test
  void copyAndSnapshotRoundTrip() {
    Position kiwipete =
        Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
    MutablePosition mutable = MutablePosition.copyOf(kiwipete);
    assertEquals(kiwipete, mutable.snapshot());
    assertEquals(Zobrist.of(kiwipete), mutable.zobristKey());
  }

  @Test
  void deepWalksRestoreEverything() {
    Position kiwipete =
        Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
    MutablePosition mutable = MutablePosition.copyOf(kiwipete);
    walk(mutable, 2);
    assertEquals(kiwipete, mutable.snapshot());
    assertEquals(Zobrist.of(kiwipete), mutable.zobristKey());
  }

  private static void walk(MutablePosition position, int depth) {
    if (depth == 0) {
      return;
    }
    for (Move move : MoveGenerator.legalMoves(position.snapshot())) {
      MutablePosition.Undo undo = position.make(move);
      walk(position, depth - 1);
      position.unmake(move, undo);
    }
  }
}
