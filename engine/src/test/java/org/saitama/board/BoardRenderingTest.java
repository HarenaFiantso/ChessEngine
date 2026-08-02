package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BoardRenderingTest {

  @Test
  void rendersFromWhitesPerspectiveWithFenSymbols() {
    Board board =
        Board.builder()
            .put(Square.E1, Piece.WHITE_KING)
            .put(Square.E8, Piece.BLACK_KING)
            .put(Square.E4, Piece.WHITE_PAWN)
            .put(Square.C6, Piece.BLACK_KNIGHT)
            .build();
    String expected =
        """
        8  . . . . k . . .
        7  . . . . . . . .
        6  . . n . . . . .
        5  . . . . . . . .
        4  . . . . P . . .
        3  . . . . . . . .
        2  . . . . . . . .
        1  . . . . K . . .

           a b c d e f g h
        """;
    assertEquals(expected, board.toString());
  }
}
