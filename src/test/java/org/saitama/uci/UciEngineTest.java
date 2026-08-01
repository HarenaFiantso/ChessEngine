package org.saitama.uci;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.saitama.board.Move;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;

class UciEngineTest {

  private static List<String> dialogue(String script) throws IOException {
    List<String> answers = new ArrayList<>();
    new UciEngine(new StringReader(script), answers::add).run();
    return answers;
  }

  @Test
  void introducesItselfOnUci() throws IOException {
    List<String> answers = dialogue("uci\nquit\n");
    assertTrue(answers.contains("id name Saitama 0.1"));
    assertEquals("uciok", answers.getLast());
  }

  @Test
  void confirmsReadiness() throws IOException {
    assertEquals(List.of("readyok"), dialogue("isready\nquit\n"));
  }

  @Test
  void answersGoWithLegalBestMoves() throws IOException {
    List<String> answers = dialogue("position startpos moves e2e4 e7e5\ngo depth 2\nquit\n");
    String bestmove = answers.getLast();
    assertTrue(bestmove.startsWith("bestmove "));
    var position = Fen.parse("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2");
    Move move = UciMoves.parse(bestmove.substring("bestmove ".length()), position);
    assertTrue(MoveGenerator.legalMoves(position).contains(move));
  }

  @Test
  void announcesForcedMates() throws IOException {
    List<String> answers =
        dialogue("position fen 6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1\ngo depth 2\nquit\n");
    assertTrue(answers.stream().anyMatch(line -> line.contains("score mate 1")));
    assertEquals("bestmove a1a8", answers.getLast());
  }

  @Test
  void answersFinishedGamesWithTheNullMove() throws IOException {
    List<String> answers =
        dialogue(
            """
            position fen rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3
            go depth 2
            quit
            """);
    assertEquals("bestmove 0000", answers.getLast());
  }

  @Test
  void playsCastlingAndPromotionHistories() throws IOException {
    assertDoesNotThrow(
        () ->
            dialogue(
                """
                position startpos moves e2e4 e7e5 g1f3 b8c6 f1c4 f8c5 e1g1
                position fen 4k3/P7/8/8/8/8/8/4K3 w - - 0 1 moves a7a8q
                quit
                """));
  }

  @Test
  void rejectsIllegalHistoriesAndKeepsServing() throws IOException {
    List<String> answers = dialogue("position startpos moves e2e5\nisready\nquit\n");
    assertTrue(answers.stream().anyMatch(line -> line.startsWith("info string rejected")));
    assertEquals("readyok", answers.getLast());
  }

  @Test
  void ignoresUnknownCommands() throws IOException {
    assertEquals(List.of("readyok"), dialogue("xyzzy\nisready\nquit\n"));
  }
}
