# Move Generation and Perft

This document explains how legal moves are produced and how the engine proves
they are the right ones.

## Two stages: pseudo-legal, then filter

Piece patterns first generate pseudo-legal moves: they respect blockers and
captures but ignore whether the mover's own king ends up attacked. A legality
filter then applies each candidate to the position and discards it if
`Attacks.isInCheck` reports the mover's king attacked afterwards.

The filter's power is that it needs no chess knowledge of its own. Absolute
pins, discovered checks, and the notorious en passant pin (where capturing
removes two pawns from one rank and opens a rook's line to the king) all fall
out of asking the real resulting board. Special-casing any of them would mean
reimplementing, in reverse, logic that apply already has.

The price is one full position copy per candidate move. That cost is real and
deliberate: correctness first, and the perft suite now exists precisely so
that faster designs (make/unmake mutation, pin-aware generation that skips
the filter, staged capture generation) can be introduced later with proof
they change nothing.

## Per-piece patterns

- Knights and kings probe fixed offsets; landing on a friendly piece is the
  only rejection.
- Bishops, rooks, and queens share one ray walker parameterized by direction
  set; a queen is the union of rook and bishop rays.
- Pawns concentrate most of the rules: color-asymmetric direction, pushes
  blocked by any occupant, double pushes only from the start rank through an
  empty square, diagonal-only captures, en passant against the position's
  recorded target, and promotion expanding every last-rank arrival into four
  moves.
- Castling is generated only when the right survives, the rook stands on its
  corner, the path is empty, and the king neither starts from nor crosses an
  attacked square; the landing square is left to the shared filter. Queenside
  castling needs b1 (or b8) empty but not safe, a classic engine bug source,
  pinned by a dedicated test.

## Perft: the correctness oracle

Perft(depth) counts leaf nodes of the legal move tree. The counts for
standard positions are mathematically established and published, so matching
them audits generation, application, and filtering as one system. The suite
covers the starting position to depth four (197,281 nodes), the Kiwipete
position built to stress castling and checks, the endgame that hides an en
passant pin, and three further stress positions covering promotions and
underpromotions.

The counter is deliberately naive: it applies every move at every node and
never shortcuts. Bulk counting (returning the move-list size at depth one)
and hashing repeated subtrees are optimizations for later; an oracle should
be as simple as possible while it is the thing other code is measured
against.

## References

- Chess Programming Wiki, Perft and Perft Results:
  https://www.chessprogramming.org/Perft
  https://www.chessprogramming.org/Perft_Results
- Chess Programming Wiki, Move Generation:
  https://www.chessprogramming.org/Move_Generation
