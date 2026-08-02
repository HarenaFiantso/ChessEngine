package org.saitama.gui;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.saitama.board.Piece;
import org.saitama.board.PositionView;
import org.saitama.board.Square;

/**
 * Renders the sixty-four squares from white's perspective and reports clicks square by square.
 *
 * <p>Pieces are drawn with the Cburnett piece set, the standard chess artwork familiar from
 * Wikipedia and Lichess, shipped as image resources. The view holds no game knowledge: it paints
 * whatever position it is handed and forwards clicks to whoever owns the rules.
 */
final class BoardView extends GridPane {

  private static final int SQUARE_SIZE = 100;
  private static final int PIECE_SIZE = 92;
  private static final int FILES = 8;
  private static final int RANKS = 8;
  private static final String LIGHT = "#f0d9b5";
  private static final String DARK = "#b58863";
  private static final String SELECTED = "#f6f375";
  private static final String TARGET = "#9bc6a2";

  private static final Map<Piece, Image> SPRITES = loadSprites();

  private final Map<Square, StackPane> cells = new EnumMap<>(Square.class);
  private final Map<Square, ImageView> sprites = new EnumMap<>(Square.class);

  BoardView(Consumer<Square> onClick) {
    for (Square square : Square.values()) {
      ImageView sprite = new ImageView();
      sprite.setFitWidth(PIECE_SIZE);
      sprite.setFitHeight(PIECE_SIZE);
      sprite.setPreserveRatio(true);
      StackPane cell = new StackPane(sprite);
      cell.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
      cell.setAlignment(Pos.CENTER);
      cell.setOnMouseClicked(event -> onClick.accept(square));
      cells.put(square, cell);
      sprites.put(square, sprite);
      add(cell, square.index() % FILES, RANKS - 1 - square.index() / FILES);
    }
  }

  /** Paints {@code position} with {@code selected} and its {@code targets} tinted. */
  void render(PositionView position, Square selected, Set<Square> targets) {
    for (Square square : Square.values()) {
      sprites.get(square).setImage(position.pieceOn(square).map(SPRITES::get).orElse(null));
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

  private static Map<Piece, Image> loadSprites() {
    Map<Piece, Image> loaded = new EnumMap<>(Piece.class);
    for (Piece piece : Piece.values()) {
      String name = "pieces/" + spriteName(piece);
      URL resource = BoardView.class.getResource(name);
      if (resource == null) {
        throw new IllegalStateException("Missing sprite " + name);
      }
      loaded.put(piece, new Image(resource.toExternalForm()));
    }
    return loaded;
  }

  private static String spriteName(Piece piece) {
    return switch (piece) {
      case WHITE_KING -> "wk.png";
      case WHITE_QUEEN -> "wq.png";
      case WHITE_ROOK -> "wr.png";
      case WHITE_BISHOP -> "wb.png";
      case WHITE_KNIGHT -> "wn.png";
      case WHITE_PAWN -> "wp.png";
      case BLACK_KING -> "bk.png";
      case BLACK_QUEEN -> "bq.png";
      case BLACK_ROOK -> "br.png";
      case BLACK_BISHOP -> "bb.png";
      case BLACK_KNIGHT -> "bn.png";
      case BLACK_PAWN -> "bp.png";
    };
  }
}
