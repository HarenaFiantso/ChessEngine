# Attack Detection

This document explains how the engine decides whether a square is attacked,
why that question precedes move generation, and how the answer will evolve.

## Why this primitive comes before move generation

Three consumers need "is square S attacked by side C" answered:

- Check detection: is the king's square attacked by the opponent?
- Castling legality: the king may not castle out of, through, or into an
  attacked square.
- Legal move filtering: a move is illegal if the mover's king is attacked
  afterwards.

The second consumer creates an ordering constraint. If attack detection were
implemented by generating the attacker's moves and testing whether any lands
on S, then move generation would need attack detection (for castling) while
attack detection needed move generation. Breaking the cycle is simple: answer
the question without ever building a move list.

## The reverse-perspective test

Stand on the square in question and project every movement pattern in
reverse; the attacker is found where the pattern says it would have to stand:

- Pawns: the two squares diagonally toward the attacker's side.
- Knights: the eight jump offsets, unaffected by any blockers.
- Kings: the eight neighboring squares.
- Sliders: walk each of the eight rays outward; the first occupied square on
  a ray decides. An enemy rook or queen on an orthogonal ray, or an enemy
  bishop or queen on a diagonal ray, means attacked; anything else blocks.

The work is bounded by a small constant: at most 2 + 8 + 8 probes plus at
most 27 ray squares, regardless of position complexity. The alternative,
generate-and-test, costs proportional to the attacker's mobility and reaches
the circularity above.

Note what the test deliberately ignores: whether the attacking piece is
pinned, and whose turn it is. Attack is a geometric fact about the board. A
pinned piece still gives check and still forbids the enemy king its square;
pins constrain the pinned piece's own moves, which is legality's department.

## Layering

`Attacks` lives in `org.saitama.rules`, which depends on `org.saitama.board`
and never the reverse. This is why `Position` has no `isInCheck` method: the
model stays free of rules logic, and the rules stay free to grow without
fattening the model. `isInCheck(Board, Color)` exists alongside
`isInCheck(Position)` because the legality filter must ask about the side
that just moved, not the side to move.

Finding the king currently scans the 64 squares. With bitboards the scan
becomes a single lowest-bit lookup on the king bitboard, and the ray walks
become precomputed attack tables (knight and king masks, magic bitboards for
sliders). The public questions will not change; only their cost will.

## References

- Chess Programming Wiki, Square Attacked By:
  https://www.chessprogramming.org/Square_Attacked_By
- Chess Programming Wiki, Checks and Pinned Pieces:
  https://www.chessprogramming.org/Checks_and_Pinned_Pieces_(Bitboards)
