package org.saitama.bench;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.saitama.board.Move;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.board.Zobrist;
import org.saitama.evaluation.ClassicalEvaluator;
import org.saitama.evaluation.Evaluator;
import org.saitama.fen.Fen;
import org.saitama.rules.MoveGenerator;
import org.saitama.rules.Perft;

/**
 * Measures the engine's hot primitives so optimization claims carry numbers instead of adjectives.
 *
 * <p>The middlegame position is Kiwipete, dense with pieces and tactics, so the costs measured here
 * resemble what search actually pays per node.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EngineBenchmarks {

  private static final String KIWIPETE =
      "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

  private Position startingPosition;
  private Position middlegame;
  private Move pawnPush;
  private Evaluator evaluator;

  /** Parses the fixed positions once per trial. */
  @Setup
  public void prepare() {
    startingPosition = Fen.parse(Fen.STARTING);
    middlegame = Fen.parse(KIWIPETE);
    pawnPush = new Move.Normal(Square.E2, Square.E4);
    evaluator = new ClassicalEvaluator();
  }

  /** Full legal move generation in a dense middlegame. */
  @Benchmark
  public List<Move> legalMoves() {
    return MoveGenerator.legalMoves(middlegame);
  }

  /** One move applied to the starting position. */
  @Benchmark
  public Position applyMove() {
    return startingPosition.apply(pawnPush);
  }

  /** Static evaluation of the middlegame. */
  @Benchmark
  public int evaluate() {
    return evaluator.evaluate(middlegame);
  }

  /** Position hashing of the middlegame. */
  @Benchmark
  public long zobrist() {
    return Zobrist.of(middlegame);
  }

  /** The rules pipeline end to end: perft three from the start. */
  @Benchmark
  public long perftThree() {
    return Perft.count(startingPosition, 3);
  }
}
