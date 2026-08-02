package org.saitama.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.saitama.board.Move;
import org.saitama.board.Piece;
import org.saitama.board.PieceType;
import org.saitama.board.Position;
import org.saitama.board.Square;
import org.saitama.fen.Fen;
import org.saitama.rules.Game;
import org.saitama.rules.GameStatus;
import org.saitama.rules.MoveGenerator;

/**
 * The interaction state a board interface needs: a game, a selected square, and a pending
 * promotion.
 *
 * <p>This is the view's brain, deliberately free of any toolkit type so the click-to-move rules are
 * provable with plain unit tests. Clicks arrive one square at a time and the session answers what
 * happened: a piece was selected, the selection cleared, a move was played, or a promotion needs a
 * piece choice before the move can complete. Engine moves enter through {@link #play}, which
 * validates them like any other move.
 */
public final class GameSession {

  /** What a click on a square did to the session. */
  public enum Response {
    IGNORED,
    CLEARED,
    SELECTED,
    PLAYED,
    PROMOTING
  }

  private Game game;
  private Square selectedSquare;
  private Square promotionFrom;
  private Square promotionTo;

  /** Creates a session at the standard starting position. */
  public GameSession() {
    this(Fen.parse(Fen.STARTING));
  }

  /** Creates a session with the game starting from {@code position}. */
  public GameSession(Position position) {
    this.game = Game.startingWith(Objects.requireNonNull(position, "position"));
  }

  /** Returns the current position. */
  public Position position() {
    return game.position();
  }

  /** Returns the current verdict on the game. */
  public GameStatus status() {
    return game.status();
  }

  /** Returns the square the mover has selected, if any. */
  public Optional<Square> selectedSquare() {
    return Optional.ofNullable(selectedSquare);
  }

  /** Returns every legal destination of the selected piece, empty when nothing is selected. */
  public Set<Square> targets() {
    if (selectedSquare == null) {
      return Set.of();
    }
    Set<Square> destinations = new HashSet<>();
    for (Move move : MoveGenerator.legalMoves(position())) {
      if (move.from() == selectedSquare) {
        destinations.add(move.to());
      }
    }
    return Set.copyOf(destinations);
  }

  /** Returns whether a promotion move waits on a piece choice. */
  public boolean awaitingPromotionChoice() {
    return promotionFrom != null;
  }

  /** Handles a click on {@code square} and reports what it did. */
  public Response click(Square square) {
    Objects.requireNonNull(square, "square");
    if (awaitingPromotionChoice() || status().isOver()) {
      return Response.IGNORED;
    }
    Optional<Piece> occupant = position().pieceOn(square);
    if (occupant.isPresent() && occupant.get().color() == position().sideToMove()) {
      selectedSquare = square;
      return Response.SELECTED;
    }
    if (selectedSquare != null && targets().contains(square)) {
      List<Move> matching = movesBetween(selectedSquare, square);
      if (matching.size() > 1) {
        promotionFrom = selectedSquare;
        promotionTo = square;
        selectedSquare = null;
        return Response.PROMOTING;
      }
      game = game.play(matching.getFirst());
      selectedSquare = null;
      return Response.PLAYED;
    }
    if (selectedSquare != null) {
      selectedSquare = null;
      return Response.CLEARED;
    }
    return Response.IGNORED;
  }

  /** Completes the pending promotion with {@code choice}. */
  public void promote(PieceType choice) {
    Objects.requireNonNull(choice, "choice");
    if (!awaitingPromotionChoice()) {
      throw new IllegalStateException("No promotion is pending");
    }
    Move move = new Move.Promotion(promotionFrom, promotionTo, choice);
    game = game.play(move);
    promotionFrom = null;
    promotionTo = null;
  }

  /** Abandons the pending promotion, as when its dialog is dismissed. */
  public void abandonPromotion() {
    promotionFrom = null;
    promotionTo = null;
  }

  /** Plays {@code move} directly, the engine's entrance; legality is validated by the game. */
  public void play(Move move) {
    Objects.requireNonNull(move, "move");
    game = game.play(move);
    selectedSquare = null;
  }

  /** Returns the session to a fresh starting position. */
  public void reset() {
    game = Game.startingWith(Fen.parse(Fen.STARTING));
    selectedSquare = null;
    promotionFrom = null;
    promotionTo = null;
  }

  private List<Move> movesBetween(Square from, Square to) {
    List<Move> matching = new ArrayList<>();
    for (Move move : MoveGenerator.legalMoves(position())) {
      if (move.from() == from && move.to() == to) {
        matching.add(move);
      }
    }
    return matching;
  }
}
