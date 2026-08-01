package org.saitama.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.fen.Fen;

class ClassicalEvaluatorTest {

  private final Evaluator evaluator = new ClassicalEvaluator();

  @Test
  void theStartingPositionIsBalanced() {
    assertEquals(0, evaluator.evaluate(Fen.parse(Fen.STARTING)));
  }

  @Test
  void centralizedKnightsOutscoreCorneredOnes() {
    int centered = evaluator.evaluate(Fen.parse("4k3/8/8/8/4N3/8/8/4K3 w - - 0 1"));
    int cornered = evaluator.evaluate(Fen.parse("4k3/8/8/8/8/8/8/N3K3 w - - 0 1"));
    assertTrue(centered > cornered);
  }

  @Test
  void advancedCentralPawnsOutscoreHomeSquares() {
    int advanced = evaluator.evaluate(Fen.parse("4k3/8/8/8/4P3/8/8/4K3 w - - 0 1"));
    int home = evaluator.evaluate(Fen.parse("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"));
    assertTrue(advanced > home);
  }

  @Test
  void tablesMirrorSymmetrically() {
    int whiteKnight = evaluator.evaluate(Fen.parse("4k3/8/8/8/8/2N5/8/4K3 w - - 0 1"));
    int blackKnight = evaluator.evaluate(Fen.parse("4k3/8/2n5/8/8/8/8/4K3 b - - 0 1"));
    assertEquals(whiteKnight, blackKnight);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "4k3/8/8/3q4/8/2N5/8/4K3",
        "r3k3/pp6/8/8/8/8/8/4K2R",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
      })
  void perspectiveFlipsTheSign(String placement) {
    int whiteView = evaluator.evaluate(Fen.parse(placement + " w - - 0 1"));
    int blackView = evaluator.evaluate(Fen.parse(placement + " b - - 0 1"));
    assertEquals(whiteView, -blackView);
  }
}
