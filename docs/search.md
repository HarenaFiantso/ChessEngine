# Search

How the engine looks ahead.

## Negamax

Minimax says: my best move maximizes the score I get assuming the opponent
replies with their own best move. Because evaluation is always mover-relative,
both players' decisions become the same operation, maximize the negation of
the child, and one code path serves both sides. `NegamaxSearch` is that
formulation, full width, no shortcuts: the reference implementation everything
faster must match.

Order of concerns at each node, and why it matters:

1. No legal moves ends the game: minus (MATE - ply) in check, zero otherwise.
   Terminal detection precedes the depth check so mate on the horizon is
   never mistaken for a quiet leaf. Subtracting the ply makes nearer mates
   score higher, so the engine finishes games instead of shuffling.
2. A halfmove clock at one hundred scores zero, matching the adjudicator.
3. Depth zero hands the position to the evaluator.

## Alpha-beta

The window [alpha, beta] carries what both sides are already guaranteed
elsewhere in the tree: alpha, the score the mover can force; beta, the score
above which the opponent will simply avoid the whole line. One reply scoring
at or above beta refutes a move, and its remaining replies need no
examination. The implementation is fail-soft, returning the best score seen
even beyond the window, which costs nothing and gives future callers tighter
information.

The two claims that make pruning trustworthy are tests, not prose: identical
scores to negamax across test positions, and under half the node count even
with unordered moves. Both algorithms report their node counts precisely so
such claims stay measurable.

## What is deliberately absent

Move ordering (searching likely-best moves first is what turns alpha-beta
from good to near-optimal), quiescence search (evaluating only quiet
positions to fix the horizon effect), iterative deepening, and transposition
tables. Each arrives as its own measured step; the node counter in
SearchResult is the instrument those measurements will use.

## References

- Chess Programming Wiki, Negamax, Alpha-Beta, Move Ordering, Quiescence:
  https://www.chessprogramming.org/Negamax
  https://www.chessprogramming.org/Alpha-Beta
  https://www.chessprogramming.org/Move_Ordering
  https://www.chessprogramming.org/Quiescence_Search
