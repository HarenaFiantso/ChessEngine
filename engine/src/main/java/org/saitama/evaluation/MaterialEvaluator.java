package org.saitama.evaluation;

import java.util.Objects;
import java.util.Optional;
import org.saitama.board.Color;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.PositionView;
import org.saitama.board.Square;

/**
 * Counts material with the conventional centipawn values: pawn 100, knight 320, bishop 330, rook
 * 500, queen 900. Kings carry no value because both are always present.
 */
public final class MaterialEvaluator implements Evaluator {

  @Override
  public int evaluate(PositionView position) {
    Objects.requireNonNull(position, "position");
    int whiteMinusBlack = 0;
    for (Square square : Square.values()) {
      Optional<Piece> occupant = position.pieceOn(square);
      if (occupant.isPresent()) {
        Piece piece = occupant.get();
        int value = value(piece.type());
        whiteMinusBlack += piece.color() == Color.WHITE ? value : -value;
      }
    }
    return position.sideToMove() == Color.WHITE ? whiteMinusBlack : -whiteMinusBlack;
  }

  static int value(PieceType type) {
    return switch (type) {
      case PAWN -> 100;
      case KNIGHT -> 320;
      case BISHOP -> 330;
      case ROOK -> 500;
      case QUEEN -> 900;
      case KING -> 0;
    };
  }
}
