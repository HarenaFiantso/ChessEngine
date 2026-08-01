package org.saitama.evaluation;

import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Color;
import org.saitama.board.Piece;
import org.saitama.board.Position;
import org.saitama.board.Square;

/**
 * Classical handcrafted evaluation: material plus piece-square bonuses.
 *
 * <p>This is the evaluator the engine plays with. Material dominates; the tables break ties in
 * favor of centralized pieces, advanced pawns, and sheltered kings. Refinements such as tapered
 * middlegame-to-endgame interpolation, pawn structure, and mobility are future terms with a
 * benchmark suite waiting to justify them.
 */
public final class ClassicalEvaluator implements Evaluator {

  @Override
  public int evaluate(Position position) {
    Objects.requireNonNull(position, "position");
    int whiteMinusBlack = 0;
    for (Square square : Square.values()) {
      Optional<Piece> occupant = position.board().pieceOn(square);
      if (occupant.isPresent()) {
        Piece piece = occupant.get();
        int worth =
            MaterialEvaluator.value(piece.type())
                + PieceSquareTables.bonus(piece.type(), piece.color(), square);
        whiteMinusBlack += piece.color() == Color.WHITE ? worth : -worth;
      }
    }
    return position.sideToMove() == Color.WHITE ? whiteMinusBlack : -whiteMinusBlack;
  }
}
