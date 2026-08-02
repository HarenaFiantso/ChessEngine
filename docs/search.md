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

Captures order themselves by inspection, but most positions are quiet, and
ordering quiet moves needs memory of what worked. Two rememberers carry
it. Killer moves are the last two quiet refuters at each ply: sibling
nodes at one ply face the same threats, so the move that just refuted one
sibling very often refutes the next, and trying it immediately after the
captures collects the cutoff almost for free. The history table is the
statistical complement, global rather than ply-local: every quiet refuter
credits its side, origin, and destination with the square of the depth it
refuted, so success at expensive nodes counts for more, and the
accumulated credit orders the remaining quiet moves. Both tables survive
across searches like the transposition table, so ordering knowledge keeps
compounding while the game stays in similar territory.

The payoff concentrates exactly where the old ordering was blind. At depth
six from the start position, a quiet opening with barely a capture in
sight, the tree shrank from 103842 nodes to 21230, five times smaller; the
tactical Kiwipete middlegame, already well served by victim-and-attacker
ranking, gave up eleven percent; scores and moves were identical
throughout. Ordering never changes the answer, only how fast it arrives,
which is why the equivalence proofs needed no re-pinning.

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
ceiling, a move-time budget, both, or neither, where unlimited limits leave
the stop to an external signal, the shape of go infinite. The deepening
search accepts that signal alongside its limits so another thread can end
it mid-flight without costing the guaranteed depth-one move. Allocating a
budget from a full game clock is policy that arrives with the UCI layer
itself.

## Aspiration windows

Deepening funds a second bet. Each iteration knows the previous score, and
scores rarely swing wildly between consecutive depths, so instead of an
unbounded window the next iteration aspires to a narrow one, half a pawn
to either side of the last answer. Alpha-beta prunes in proportion to how
much is already ruled out, and a narrow window rules out almost
everything, so the whole tree cuts harder. The price appears when the bet
loses: a fail-soft score landing on or outside an edge is only a bound,
not an answer, and the iteration must re-search with the full window. Mate
scores are never aspired around, because they measure distance rather
than evaluation, and a centipawn margin around a mate distance is
meaningless.

The root gained an explicit window for this, with the fail-soft contract
tests pin directly: a score strictly inside the window equals the
full-window answer, a score at an edge or beyond is a bound in that
direction. The repair discipline is proven the same way the rest of the
search is: an aspiring deepening over the exact configuration still
produces exact scores, including across the depth where a winning
position's evaluation jumps to a mate distance and the window must miss.
Measured at depth six with all pruning on: six to thirteen percent fewer
nodes with identical scores and moves across opening, middlegame, and
endgame positions.

## Walking the tree with make and unmake

The pruning search walks the whole tree on one mutable scratch copy of the
root: each candidate move is made, the mover's king is tested for exposure,
the child is searched, and the move is unmade on backtrack. Nothing per node
copies a board, and the transposition table reads the incrementally
maintained Zobrist key instead of rescanning sixty-four squares. Legality is
discovered move by move rather than up front, which relocates terminal
detection: checkmate and stalemate reveal themselves only after the move
loop finds nothing legal to play, and the quiescence stand-pat must first
prove some legal move exists, because a stalemate is a draw no matter how
good the static evaluation looks. The probe is cheap: it stops at the first
legal move, usually the first one tried.

An aborted search abandons the scratch copy mid-line. That is safe, not
sloppy: the aborted iteration is discarded entirely and the next search
begins from a fresh copy of its root. NegamaxSearch deliberately stays on
the immutable apply path; the equivalence tests thereby prove the mutable
fast path against an oracle that cannot share its bugs.

## Null move pruning

The first speculative cut, and a shift in kind: everything before it kept
the search exact, this trades a sliver of exactness for a large slice of
the tree. The bet is the null move observation: in almost every chess
position, moving improves matters. So before searching a node's moves,
give the opponent a free extra move and search the result shallower with
a null window. If the mover still stands at or above beta after handing
over the tempo, the real position, where the mover does get to move, will
almost certainly clear beta too, and the node is pruned on the spot.

The bet is off exactly where the observation fails. In check, passing
would be illegal. In pawn-and-king endgames zugzwang is real, moving can
only hurt, so the cut requires the mover to own at least one piece.
Near mate scores the arithmetic of mate distances cannot be trusted from
a reduced search, so those windows are exempt, which conveniently keeps
open-window principal variation nodes unpruned. And two consecutive null
moves would compare doing nothing with doing nothing, so a null search
may not open with another null.

Because the cut is speculative, the testing contract changed shape: the
equivalence proof against the exhaustive reference now pins a
configuration with null move pruning disabled, exactly as it already
pinned a disabled transposition table, and the speculative configuration
is held to what it actually promises. Measured on Kiwipete at fixed depth
five: 42 percent fewer nodes, same score, same move. At depth three it
never fires and at depth four it is a wash, which the tests record
honestly by asserting at the depth where the technique starts paying.

## Late move reductions

The third speculative technique closes the loop the ordering work opened.
If ordering is trustworthy, then by the time the remembered best move, the
captures, the killers, and the credited quiets have been searched, the
moves at the back of the list almost never raise alpha. Late move
reductions act on that: quiet stragglers, beyond the first three legal
moves at a node, are first searched one ply shallower with a null window,
a cheap test of the hypothesis that they change nothing. A straggler that
surprises, scoring above alpha even reduced, immediately earns the full
depth and full window, so a mistaken reduction costs one extra shallow
search rather than the truth.

The cut is off wherever shallowness lies: for captures and promotions,
whose tactics are the point; in check and for checking moves, where
forcing lines need full depth; and below depth three, where there is
nothing to reduce. It waited deliberately for the killer and history work,
because reducing late moves is only safe once late means unpromising
rather than merely unlucky in generation order. Measured at depth six the
complement is visible: the tactical Kiwipete middlegame, where quiet
stragglers abound, gave up 47 percent of its nodes, the endgame 28, the
already-tiny quiet opening 14, with identical scores and moves throughout.

## What is deliberately absent

Static exchange evaluation for capture ordering, and depth-adaptive
reduction amounts. Each arrives as its own measured step; the node counter
in SearchResult is the instrument those measurements use.

## References

- Chess Programming Wiki, Negamax, Alpha-Beta, Move Ordering, Quiescence:
  https://www.chessprogramming.org/Negamax
  https://www.chessprogramming.org/Alpha-Beta
  https://www.chessprogramming.org/Move_Ordering
  https://www.chessprogramming.org/Quiescence_Search
