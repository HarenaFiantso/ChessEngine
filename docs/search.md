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

## Move ordering

Pruning efficiency depends entirely on meeting a strong move early, so
alpha-beta sorts candidates before searching: captures first, ranked by most
valuable victim then least valuable attacker, then promotions, then quiet
moves, with the transposition table's remembered best move ahead of them
all. Measured on the depth-four opening search, ordering alone cut nodes
from 8176 to 7102; its real value shows in tactical positions and inside
quiescence trees, which are nothing but captures.

## Quiescence

A fixed-depth search may evaluate a position in the middle of a capture
sequence and believe a queen won when the recapture sits one ply past the
horizon. At depth zero both searches therefore resolve captures before
judging the leaf: stand pat on the static evaluation, otherwise try captures
recursively, and when in check search every evasion with no standing pat.
The engine consequently declines a poisoned pawn even at depth one.
Quiescence lives in both algorithms so the equivalence tests keep their
meaning; and it taught a lesson worth recording: without cutoffs, resolving
capture trees exhaustively takes minutes on positions the pruned search
finishes in milliseconds, so the comparison tests feed the exhaustive
reference deliberately shallow depths.

## Iterative deepening

The search runs depth one, then two, then deeper, until a limit calls time,
and answers from the deepest fully completed iteration. Repeating shallow
work costs less than it seems: exponential tree growth means all previous
iterations together cost a fraction of the final one, and each iteration
fills the transposition table whose remembered best moves make the next far
cheaper than a cold start. The reward is the anytime property real play
requires: a fully searched move is always in hand when the clock fires.

Abortion is cooperative and strict. The recursion polls a stop signal at
every node and unwinds through an exception; an interrupted iteration is
discarded entirely, because a partial alpha-beta pass may not have finished
refuting its current best move. Depth one always completes, so a legal move
is guaranteed under any budget, and a forced mate stops the deepening early.
The clock is injected as a supplier, so tests drive time deterministically.

SearchLimits speaks the vocabulary the UCI go command maps onto: a depth
ceiling, a move-time budget, or both. Allocating a budget from a full game
clock is policy that arrives with the UCI layer itself.

## What is deliberately absent

Aspiration windows, null-move pruning, and late move reductions. Each
arrives as its own measured step; the node counter in SearchResult is the
instrument those measurements use.

## References

- Chess Programming Wiki, Negamax, Alpha-Beta, Move Ordering, Quiescence:
  https://www.chessprogramming.org/Negamax
  https://www.chessprogramming.org/Alpha-Beta
  https://www.chessprogramming.org/Move_Ordering
  https://www.chessprogramming.org/Quiescence_Search
