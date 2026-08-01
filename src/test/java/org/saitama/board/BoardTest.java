package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BoardTest {

  @ParameterizedTest
  @EnumSource(Square.class)
  void emptyBoardHasNoPieceAnywhere(Square square) {
    assertEquals(Optional.empty(), Board.empty().pieceOn(square));
  }

  @Test
  void builderPlacesPieces() {
    Board board = Board.builder().put(Square.E4, Piece.WHITE_PAWN).build();
    assertEquals(Optional.of(Piece.WHITE_PAWN), board.pieceOn(Square.E4));
    assertEquals(Optional.empty(), board.pieceOn(Square.E5));
  }

  @Test
  void builderReplacesEarlierPlacementOnTheSameSquare() {
    Board board =
        Board.builder().put(Square.E4, Piece.WHITE_PAWN).put(Square.E4, Piece.BLACK_QUEEN).build();
    assertEquals(Optional.of(Piece.BLACK_QUEEN), board.pieceOn(Square.E4));
  }

  @Test
  void builtBoardIsUnaffectedByLaterBuilderUse() {
    Board.Builder builder = Board.builder();
    Board before = builder.build();
    builder.put(Square.A1, Piece.WHITE_ROOK);
    assertEquals(Optional.empty(), before.pieceOn(Square.A1));
  }

  @Test
  void boardsWithTheSamePlacementAreEqual() {
    Board first = Board.builder().put(Square.G8, Piece.BLACK_KNIGHT).build();
    Board second = Board.builder().put(Square.G8, Piece.BLACK_KNIGHT).build();
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void boardsWithDifferentPlacementsAreNotEqual() {
    Board empty = Board.empty();
    Board occupied = Board.builder().put(Square.D5, Piece.WHITE_BISHOP).build();
    assertNotEquals(empty, occupied);
  }

  @Test
  void rejectsNullArguments() {
    assertThrows(NullPointerException.class, () -> Board.empty().pieceOn(null));
    assertThrows(NullPointerException.class, () -> Board.builder().put(null, Piece.WHITE_KING));
    assertThrows(NullPointerException.class, () -> Board.builder().put(Square.E1, null));
  }

  @Test
  void equalityIsReflexiveAndRejectsOtherTypes() {
    Board board = Board.empty();
    assertEquals(board, board);
    assertNotEquals(board, new Object());
  }
}
