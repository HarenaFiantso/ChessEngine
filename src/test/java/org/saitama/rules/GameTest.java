package org.saitama.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.board.Square;
import org.saitama.fen.Fen;

class GameTest {

  private static final List<Move> KNIGHT_SHUFFLE =
      List.of(
          new Move.Normal(Square.G1, Square.F3),
          new Move.Normal(Square.G8, Square.F6),
          new Move.Normal(Square.F3, Square.G1),
          new Move.Normal(Square.F6, Square.G8));

  private static Game play(Game game, List<Move> moves) {
    for (Move move : moves) {
      game = game.play(move);
    }
    return game;
  }

  @Test
  void detectsThreefoldRepetitionAcrossShuffles() {
    Game game = Game.startingWith(Fen.parse(Fen.STARTING));
    game = play(game, KNIGHT_SHUFFLE);
    assertEquals(GameStatus.ONGOING, game.status());
    game = play(game, KNIGHT_SHUFFLE);
    assertEquals(GameStatus.DRAW_BY_REPETITION, game.status());
    assertTrue(game.status().isDraw());
  }

  @Test
  void checkmateOutranksRepetition() {
    Game game = Game.startingWith(Fen.parse(Fen.STARTING));
    game =
        play(
            game,
            List.of(
                new Move.Normal(Square.F2, Square.F3),
                new Move.Normal(Square.E7, Square.E5),
                new Move.Normal(Square.G2, Square.G4),
                new Move.Normal(Square.D8, Square.H4)));
    assertEquals(GameStatus.CHECKMATE, game.status());
  }

  @Test
  void rejectsIllegalMoves() {
    Game game = Game.startingWith(Fen.parse(Fen.STARTING));
    Move illegal = new Move.Normal(Square.E2, Square.E5);
    assertThrows(IllegalArgumentException.class, () -> game.play(illegal));
  }

  @Test
  void playingReturnsNewGamesAndLeavesTheOldUntouched() {
    Game before = Game.startingWith(Fen.parse(Fen.STARTING));
    Game after = before.play(new Move.Normal(Square.E2, Square.E4));
    assertEquals(Fen.STARTING, Fen.write(before.position()));
    assertEquals(
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", Fen.write(after.position()));
  }
}
