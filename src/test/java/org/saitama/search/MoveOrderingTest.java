package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;

class MoveOrderingTest {

  @Test
  void cheapAttackersOnFatVictimsComeFirst() {
    Position position = Fen.parse("k7/8/8/q2p4/1P6/8/8/3QK3 w - - 0 1");
    List<Move> ordered = MoveOrdering.byPromise(position, MoveGenerator.legalMoves(position));
    assertEquals(new Move.Normal(Square.B4, Square.A5), ordered.get(0));
    assertEquals(new Move.Normal(Square.D1, Square.D5), ordered.get(1));
  }

  @Test
  void capturesPrecedeQuietMoves() {
    Position position = Fen.parse("k7/8/8/3p4/8/8/8/3RK3 w - - 0 1");
    List<Move> ordered = MoveOrdering.byPromise(position, MoveGenerator.legalMoves(position));
    assertEquals(new Move.Normal(Square.D1, Square.D5), ordered.get(0));
  }

  @Test
  void promotionsPrecedeQuietMovesAndQueensLead() {
    Position position = Fen.parse("k7/6P1/8/8/8/8/8/4K3 w - - 0 1");
    List<Move> ordered = MoveOrdering.byPromise(position, MoveGenerator.legalMoves(position));
    assertTrue(
        ordered.get(0) instanceof Move.Promotion promotion
            && promotion.promoted() == PieceType.QUEEN);
  }

  @Test
  void enPassantCountsAsCapture() {
    Position position = Fen.parse("k7/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
    assertTrue(MoveOrdering.isCapture(position, new Move.EnPassant(Square.E5, Square.D6)));
  }
}
