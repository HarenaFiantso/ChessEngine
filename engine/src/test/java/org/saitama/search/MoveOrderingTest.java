package org.saitama.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;

class MoveOrderingTest {

  private static List<Move> ordered(MoveOrdering ordering, Position position, int ply) {
    return ordering.byPromise(position, MoveGenerator.legalMoves(position), Optional.empty(), ply);
  }

  @Test
  void cheapAttackersOnFatVictimsComeFirst() {
    Position position = Fen.parse("k7/8/8/q2p4/1P6/8/8/3QK3 w - - 0 1");
    List<Move> ordered = ordered(new MoveOrdering(), position, 0);
    assertEquals(new Move.Normal(Square.B4, Square.A5), ordered.get(0));
    assertEquals(new Move.Normal(Square.D1, Square.D5), ordered.get(1));
  }

  @Test
  void capturesPrecedeQuietMoves() {
    Position position = Fen.parse("k7/8/8/3p4/8/8/8/3RK3 w - - 0 1");
    List<Move> ordered = ordered(new MoveOrdering(), position, 0);
    assertEquals(new Move.Normal(Square.D1, Square.D5), ordered.get(0));
  }

  @Test
  void promotionsPrecedeQuietMovesAndQueensLead() {
    Position position = Fen.parse("k7/6P1/8/8/8/8/8/4K3 w - - 0 1");
    List<Move> ordered = ordered(new MoveOrdering(), position, 0);
    assertTrue(
        ordered.get(0) instanceof Move.Promotion promotion
            && promotion.promoted() == PieceType.QUEEN);
  }

  @Test
  void enPassantCountsAsCapture() {
    Position position = Fen.parse("k7/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
    assertTrue(MoveOrdering.isCapture(position, new Move.EnPassant(Square.E5, Square.D6)));
  }

  @Test
  void quietRefutersLeadTheirPlyAfterCutoffs() {
    Position position = Fen.parse(Fen.STARTING);
    MoveOrdering ordering = new MoveOrdering();
    Move killer = new Move.Normal(Square.G2, Square.G4);
    ordering.rememberCutoff(position, killer, 4, 3);
    assertEquals(killer, ordered(ordering, position, 3).getFirst());
  }

  @Test
  void killersStayLocalToTheirPly() {
    Position position = Fen.parse(Fen.STARTING);
    MoveOrdering ordering = new MoveOrdering();
    Move plyThreeRefuter = new Move.Normal(Square.A2, Square.A3);
    Move plyFiveRefuter = new Move.Normal(Square.H2, Square.H3);
    ordering.rememberCutoff(position, plyThreeRefuter, 4, 3);
    ordering.rememberCutoff(position, plyFiveRefuter, 4, 5);
    assertEquals(plyThreeRefuter, ordered(ordering, position, 3).getFirst());
    assertEquals(plyFiveRefuter, ordered(ordering, position, 5).getFirst());
  }

  @Test
  void theLastTwoKillersOfOnePlyAreRemembered() {
    Position position = Fen.parse(Fen.STARTING);
    MoveOrdering ordering = new MoveOrdering();
    Move older = new Move.Normal(Square.A2, Square.A3);
    Move newer = new Move.Normal(Square.H2, Square.H3);
    ordering.rememberCutoff(position, older, 3, 2);
    ordering.rememberCutoff(position, newer, 3, 2);
    List<Move> ordered = ordered(ordering, position, 2);
    assertEquals(newer, ordered.get(0));
    assertEquals(older, ordered.get(1));
  }

  @Test
  void historyCreditCarriesAcrossPlies() {
    Position position = Fen.parse(Fen.STARTING);
    MoveOrdering ordering = new MoveOrdering();
    Move refuter = new Move.Normal(Square.B1, Square.C3);
    ordering.rememberCutoff(position, refuter, 5, 3);
    assertEquals(refuter, ordered(ordering, position, 9).getFirst());
  }

  @Test
  void deeperRefutationsOutrankShallowerOnes() {
    Position position = Fen.parse(Fen.STARTING);
    MoveOrdering ordering = new MoveOrdering();
    Move shallow = new Move.Normal(Square.A2, Square.A3);
    Move deep = new Move.Normal(Square.D2, Square.D4);
    ordering.rememberCutoff(position, shallow, 2, 1);
    ordering.rememberCutoff(position, deep, 6, 2);
    List<Move> ordered = ordered(ordering, position, 9);
    assertTrue(ordered.indexOf(deep) < ordered.indexOf(shallow));
  }

  @Test
  void killersOutrankLosingCaptures() {
    Position position = Fen.parse("k7/8/2p5/3p4/8/8/8/3QK3 w - - 0 1");
    MoveOrdering ordering = new MoveOrdering();
    Move quiet = new Move.Normal(Square.E1, Square.E2);
    ordering.rememberCutoff(position, quiet, 4, 3);
    List<Move> ordered = ordered(ordering, position, 3);
    assertTrue(ordered.indexOf(quiet) < ordered.indexOf(new Move.Normal(Square.D1, Square.D5)));
  }

  @Test
  void losingCapturesStillPrecedeUncreditedQuietMoves() {
    Position position = Fen.parse("k7/8/2p5/3p4/8/8/8/3QK3 w - - 0 1");
    List<Move> ordered = ordered(new MoveOrdering(), position, 0);
    assertEquals(new Move.Normal(Square.D1, Square.D5), ordered.getFirst());
  }

  @Test
  void captureCutoffsLeaveTheQuietTablesAlone() {
    Position position = Fen.parse("k7/8/8/3p4/8/8/8/3RK3 w - - 0 1");
    MoveOrdering ordering = new MoveOrdering();
    ordering.rememberCutoff(position, new Move.Normal(Square.D1, Square.D5), 4, 2);
    Position start = Fen.parse(Fen.STARTING);
    assertEquals(ordered(new MoveOrdering(), start, 2), ordered(ordering, start, 2));
  }
}
