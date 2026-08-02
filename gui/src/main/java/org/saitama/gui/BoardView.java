package org.saitama.gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.saitama.board.Piece;
import org.saitama.board.PositionView;
import org.saitama.board.Square;

/**
 * Renders the sixty-four squares from white's perspective and reports clicks square by square.
 *
 * <p>Pieces are drawn with the Unicode chess glyphs, so the board needs no image assets. The view
 * holds no game knowledge: it paints whatever position it is handed and forwards clicks to whoever
 * owns the rules.
 */
final class BoardView extends GridPane {

  private static final int SQUARE_SIZE = 76;
  private static final int FILES = 8;
  private static final int RANKS = 8;
  private static final String LIGHT = "#f0d9b5";
  private static final String DARK = "#b58863";
  private static final String SELECTED = "#f6f375";
  private static final String TARGET = "#9bc6a2";

  private final Map<Square, StackPane> cells = new EnumMap<>(Square.class);
  private final Map<Square, Label> glyphs = new EnumMap<>(Square.class);

  BoardView(Consumer<Square> onClick) {
    for (Square square : Square.values()) {
      Label glyph = new Label();
      glyph.setStyle("-fx-font-size: 46px;");
      StackPane cell = new StackPane(glyph);
      cell.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
      cell.setAlignment(Pos.CENTER);
      cell.setOnMouseClicked(event -> onClick.accept(square));
      cells.put(square, cell);
      glyphs.put(square, glyph);
      add(cell, square.index() % FILES, RANKS - 1 - square.index() / FILES);
    }
  }

  /** Paints {@code position} with {@code selected} and its {@code targets} tinted. */
  void render(PositionView position, Square selected, Set<Square> targets) {
    for (Square square : Square.values()) {
      glyphs.get(square).setText(position.pieceOn(square).map(BoardView::glyph).orElse(""));
      cells.get(square).setStyle("-fx-background-color: " + fill(square, selected, targets) + ";");
    }
  }

  private static String fill(Square square, Square selected, Set<Square> targets) {
    if (square == selected) {
      return SELECTED;
    }
    if (targets.contains(square)) {
      return TARGET;
    }
    return square.isLight() ? LIGHT : DARK;
  }

  private static String glyph(Piece piece) {
    return switch (piece) {
      case WHITE_KING -> "♔";
      case WHITE_QUEEN -> "♕";
      case WHITE_ROOK -> "♖";
      case WHITE_BISHOP -> "♗";
      case WHITE_KNIGHT -> "♘";
      case WHITE_PAWN -> "♙";
      case BLACK_KING -> "♚";
      case BLACK_QUEEN -> "♛";
      case BLACK_ROOK -> "♜";
      case BLACK_BISHOP -> "♝";
      case BLACK_KNIGHT -> "♞";
      case BLACK_PAWN -> "♟";
    };
  }
}
