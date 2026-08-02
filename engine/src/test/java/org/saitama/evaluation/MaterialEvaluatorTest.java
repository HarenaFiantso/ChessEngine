package org.saitama.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.saitama.fen.Fen;

class MaterialEvaluatorTest {

  private final Evaluator evaluator = new MaterialEvaluator();

  @Test
  void balancedMaterialScoresZero() {
    assertEquals(0, evaluator.evaluate(Fen.parse(Fen.STARTING)));
  }

  @ParameterizedTest
  @CsvSource({
    "Q3k3/8/8/8/8/8/8/4K3 w - - 0 1, 900",
    "4k3/8/8/8/8/8/8/R3K3 w - - 0 1, 500",
    "4k3/8/8/8/8/8/8/RN2K3 b - - 0 1, -820",
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPP1/RNBQKBNR w KQkq - 0 1, -100"
  })
  void countsTheConventionalValues(String record, int expected) {
    assertEquals(expected, evaluator.evaluate(Fen.parse(record)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "4k3/8/8/8/8/8/8/Q3K3",
        "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",
        "4k3/8/8/8/8/8/8/4K3"
      })
  void perspectiveFlipsTheSign(String placement) {
    int whiteView = evaluator.evaluate(Fen.parse(placement + " w - - 0 1"));
    int blackView = evaluator.evaluate(Fen.parse(placement + " b - - 0 1"));
    assertEquals(whiteView, -blackView);
  }

  @Test
  void rejectsNullPositions() {
    assertThrows(NullPointerException.class, () -> evaluator.evaluate(null));
  }
}
