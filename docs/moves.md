# Moves and Move Application

This document explains how the engine represents a chess move and how a move
transforms one position into the next.

## The move model

A move is what a player states: origin, destination, and for promotions the
piece chosen. The engine models this as a sealed interface with four record
variants, one per mechanically distinct kind of move:

- `Move.Normal`: one piece travels, possibly capturing, including pawn double
  pushes.
- `Move.Promotion`: a pawn arrives on the last rank and becomes another piece.
- `Move.EnPassant`: a pawn captures by landing on the en passant target.
- `Move.Castling`: the king travels two squares and the rook follows by rule.

Sealing buys exhaustiveness: `Position.apply` switches over the hierarchy
with no default branch, so a hypothetical fifth kind would fail compilation
at every application site rather than fall through silently.

Production engines encode a move in sixteen bits (six for each square, four
for kind and promotion) because search creates and discards millions of moves
per second. That representation is a cache-density optimization, not a
different concept; it can replace these records behind the same seams once
profiling justifies it. Choosing readable records first follows the project
rule: correctness before performance, performance only with benchmarks.

## Validation in layers

Each record validates the position-independent geometry of its kind at
construction: castling knows its four king paths, en passant its single
diagonal step onto rank 6 or 3, promotion its last-rank arrival and that
kings and pawns are not promotion targets. A `Move` that exists is therefore
always shaped like a chess move.

`Position.apply` adds the checks that need the position: a friendly piece
stands on the origin, the destination holds no friendly piece, the en passant
target matches, the castling rook is on its corner. What it deliberately does
not check is legality: sliding pieces are not stopped by blockers and checks
are not detected. That discipline belongs to attack detection and move
generation, which will only ever feed legal moves to apply. Perft will then
validate the whole pipeline against known node counts.

## State bookkeeping

Applying a move produces a new immutable `Position`; nothing is mutated. The
bookkeeping rules, each pinned by a before-and-after FEN scenario test:

- The en passant target exists exactly when the move just played was a pawn
  double push, and names the square passed over.
- The halfmove clock resets on every pawn move and capture, otherwise grows.
- The fullmove number grows after black's move.
- Castling rights decay by a uniform rule: any move touching e1, h1, a1, e8,
  h8, or a8, whether leaving the square or capturing onto it, removes the
  rights anchored to that square. King moves, rook moves, and rook captures
  all fall out of the one rule, which is how production engines implement it
  as per-square masks.

Immutable application allocates a fresh board per move, which search would
feel. Real engines mutate a single board and undo moves when backtracking
(make/unmake). The choice will be revisited with measurements once perft
exists; until then the immutable form keeps every position trivially safe to
share, hash, and test.

## References

- Chess Programming Wiki, Encoding Moves:
  https://www.chessprogramming.org/Encoding_Moves
- Chess Programming Wiki, Make Move:
  https://www.chessprogramming.org/Make_Move
