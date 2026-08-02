package org.saitama.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.saitama.board.Color;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.GameStatus;

class GameSessionTest {

  @Test
  void clickingOwnPieceSelectsItAndOffersItsMoves() {
    GameSession session = new GameSession();
    assertEquals(GameSession.Response.SELECTED, session.click(Square.E2));
    assertEquals(Optional.of(Square.E2), session.selectedSquare());
    assertEquals(Set.of(Square.E3, Square.E4), session.targets());
  }

  @Test
  void clickingEmptySquaresWithNothingSelectedChangesNothing() {
    GameSession session = new GameSession();
    assertEquals(GameSession.Response.IGNORED, session.click(Square.E4));
    assertEquals(Optional.empty(), session.selectedSquare());
  }

  @Test
  void clickingEnemyPiecesWithNothingSelectedChangesNothing() {
    GameSession session = new GameSession();
    assertEquals(GameSession.Response.IGNORED, session.click(Square.E7));
  }

  @Test
  void clickingTargetsPlaysTheMove() {
    GameSession session = new GameSession();
    session.click(Square.E2);
    assertEquals(GameSession.Response.PLAYED, session.click(Square.E4));
    assertEquals(Color.BLACK, session.position().sideToMove());
    assertEquals(Optional.of(Piece.WHITE_PAWN), session.position().pieceOn(Square.E4));
    assertEquals(Optional.empty(), session.selectedSquare());
  }

  @Test
  void clickingElsewhereClearsTheSelection() {
    GameSession session = new GameSession();
    session.click(Square.E2);
    assertEquals(GameSession.Response.CLEARED, session.click(Square.E5));
    assertEquals(Optional.empty(), session.selectedSquare());
  }

  @Test
  void reselectingAnotherFriendlyPieceMovesTheSelection() {
    GameSession session = new GameSession();
    session.click(Square.E2);
    assertEquals(GameSession.Response.SELECTED, session.click(Square.G1));
    assertEquals(Optional.of(Square.G1), session.selectedSquare());
    assertEquals(Set.of(Square.F3, Square.H3), session.targets());
  }

  @Test
  void promotionsAskForThePieceBeforePlaying() {
    GameSession session = new GameSession(Fen.parse("4k3/P7/8/8/8/8/8/4K3 w - - 0 1"));
    session.click(Square.A7);
    assertEquals(GameSession.Response.PROMOTING, session.click(Square.A8));
    assertTrue(session.awaitingPromotionChoice());
    session.promote(PieceType.QUEEN);
    assertEquals(Optional.of(Piece.WHITE_QUEEN), session.position().pieceOn(Square.A8));
    assertFalse(session.awaitingPromotionChoice());
  }

  @Test
  void clicksDuringPendingPromotionsAreIgnored() {
    GameSession session = new GameSession(Fen.parse("4k3/P7/8/8/8/8/8/4K3 w - - 0 1"));
    session.click(Square.A7);
    session.click(Square.A8);
    assertEquals(GameSession.Response.IGNORED, session.click(Square.E1));
  }

  @Test
  void abandonedPromotionsLeaveTheGameUntouched() {
    GameSession session = new GameSession(Fen.parse("4k3/P7/8/8/8/8/8/4K3 w - - 0 1"));
    session.click(Square.A7);
    session.click(Square.A8);
    session.abandonPromotion();
    assertFalse(session.awaitingPromotionChoice());
    assertEquals(Optional.of(Piece.WHITE_PAWN), session.position().pieceOn(Square.A7));
  }

  @Test
  void promotingWithoutPendingMovesIsRejected() {
    GameSession session = new GameSession();
    assertThrows(IllegalStateException.class, () -> session.promote(PieceType.QUEEN));
  }

  @Test
  void engineMovesEnterThroughPlay() {
    GameSession session = new GameSession();
    session.play(new Move.Normal(Square.E2, Square.E4));
    session.play(new Move.Normal(Square.E7, Square.E5));
    assertEquals(Color.WHITE, session.position().sideToMove());
  }

  @Test
  void illegalMovesAreRejectedByTheGame() {
    GameSession session = new GameSession();
    assertThrows(
        IllegalArgumentException.class, () -> session.play(new Move.Normal(Square.E2, Square.E5)));
  }

  @Test
  void finishedGamesIgnoreFurtherClicks() {
    GameSession session = new GameSession();
    session.play(new Move.Normal(Square.F2, Square.F3));
    session.play(new Move.Normal(Square.E7, Square.E5));
    session.play(new Move.Normal(Square.G2, Square.G4));
    session.play(new Move.Normal(Square.D8, Square.H4));
    assertEquals(GameStatus.CHECKMATE, session.status());
    assertEquals(GameSession.Response.IGNORED, session.click(Square.E2));
  }

  @Test
  void resetReturnsToTheStartingPosition() {
    GameSession session = new GameSession();
    session.click(Square.E2);
    session.click(Square.E4);
    session.reset();
    assertEquals(Fen.parse(Fen.STARTING), session.position());
    assertEquals(Optional.empty(), session.selectedSquare());
  }
}
