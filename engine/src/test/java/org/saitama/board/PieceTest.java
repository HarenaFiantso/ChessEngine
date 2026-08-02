package org.saitama.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PieceTest {

  @Test
  void thereIsOnePiecePerColorAndTypeCombination() {
    assertEquals(12, Piece.values().length);
  }

  @ParameterizedTest
  @EnumSource(Piece.class)
  void ofReturnsTheSharedInstanceForItsCoordinates(Piece piece) {
    assertSame(piece, Piece.of(piece.color(), piece.type()));
  }

  @ParameterizedTest
  @CsvSource({
    "WHITE_PAWN,P",
    "WHITE_KNIGHT,N",
    "WHITE_BISHOP,B",
    "WHITE_ROOK,R",
    "WHITE_QUEEN,Q",
    "WHITE_KING,K",
    "BLACK_PAWN,p",
    "BLACK_KNIGHT,n",
    "BLACK_BISHOP,b",
    "BLACK_ROOK,r",
    "BLACK_QUEEN,q",
    "BLACK_KING,k"
  })
  void fenSymbolsFollowTheStandard(Piece piece, char symbol) {
    assertEquals(symbol, piece.fenSymbol());
  }

  @ParameterizedTest
  @EnumSource(Piece.class)
  void fenSymbolsRoundTrip(Piece piece) {
    assertSame(piece, Piece.ofFenSymbol(piece.fenSymbol()));
  }

  @ParameterizedTest
  @ValueSource(chars = {'x', 'Z', '1', ' '})
  void ofFenSymbolRejectsUnknownSymbols(char symbol) {
    assertThrows(IllegalArgumentException.class, () -> Piece.ofFenSymbol(symbol));
  }
}
