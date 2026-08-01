package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.search.TranspositionTable.NodeType;

class TranspositionTableTest {

  @Test
  void storesAndRecallsByExactKey() {
    TranspositionTable table = TranspositionTable.withCapacity(16);
    Move move = new Move.Normal(Square.E2, Square.E4);
    table.store(42, 3, 120, NodeType.EXACT, Optional.of(move));
    TranspositionTable.Entry entry = table.probe(42).orElseThrow();
    assertEquals(3, entry.depth());
    assertEquals(120, entry.score());
    assertEquals(NodeType.EXACT, entry.type());
    assertEquals(Optional.of(move), entry.bestMove());
  }

  @Test
  void slotCollisionsDoNotLeakForeignEntries() {
    TranspositionTable table = TranspositionTable.withCapacity(16);
    table.store(5, 2, 50, NodeType.EXACT, Optional.empty());
    assertEquals(Optional.empty(), table.probe(5 + 16));
  }

  @Test
  void newerEntriesReplaceOldOccupants() {
    TranspositionTable table = TranspositionTable.withCapacity(16);
    table.store(5, 2, 50, NodeType.EXACT, Optional.empty());
    table.store(5 + 16, 4, -70, NodeType.LOWER_BOUND, Optional.empty());
    assertEquals(Optional.empty(), table.probe(5));
    assertEquals(-70, table.probe(5 + 16).orElseThrow().score());
  }

  @Test
  void capacityRoundsUpToPowersOfTwo() {
    TranspositionTable table = TranspositionTable.withCapacity(9);
    for (long key = 0; key < 16; key++) {
      table.store(key, 1, (int) key, NodeType.EXACT, Optional.empty());
    }
    for (long key = 0; key < 16; key++) {
      assertTrue(table.probe(key).isPresent());
    }
  }

  @Test
  void disabledTablesStoreNothing() {
    TranspositionTable table = TranspositionTable.disabled();
    table.store(42, 3, 120, NodeType.EXACT, Optional.empty());
    assertEquals(Optional.empty(), table.probe(42));
  }

  @Test
  void rejectsSenselessCapacities() {
    assertThrows(IllegalArgumentException.class, () -> TranspositionTable.withCapacity(0));
  }
}
