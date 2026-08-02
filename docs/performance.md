# Performance Notes

Measurements, method, and the honest history of what worked and what did not.

## Method

All numbers come from JMH (`./gradlew jmh`): average time per operation,
single fork, warmed up, on the dense Kiwipete middlegame unless stated. JMH
exists because naive timing loops lie on the JVM: they measure interpretation
before the JIT warms up, and dead-code elimination can silently delete the
work being measured. Optimization claims in this repository cite these
benchmarks or do not get merged.

## Current numbers

| Operation | Cost |
|---|---|
| Position.apply (one move) | 0.034 us |
| ClassicalEvaluator.evaluate | 0.18 us |
| Zobrist.of | 0.16 us |
| MoveGenerator.legalMoves (Kiwipete) | 15 us |
| Perft(3) from the start | 2.9 ms |
| Alpha-beta depth 4, fresh table | 4.5 ms |
| Iterative deepening to depth 6 | 52 ms |

Move generation dominates everything built on it. Its cost is the legality
filter: every pseudo-legal candidate is made, answered by isInCheck (a
reverse attack scan), and unmade. Roughly fifty candidates in a middlegame
means fifty make-and-check round trips per node, plus the pseudo-legal
lists themselves.

## History

- King-square tracking, first attempt: scanning for kings in the Board
  constructor made everything slower (perft(3) 3.76ms to 5.15ms), because
  apply builds several intermediate boards through its with-er chain and
  each paid the scan. The benchmark caught the regression before it shipped.
- King-square tracking, second attempt: maintaining the two squares
  incrementally in the with-ers is constant-time and shipped: legalMoves
  16.6us to 15.2us, perft(3) 3.76ms to 3.53ms, apply unchanged.
- Move-ordering allocation trim: replacing the stream sort and stream filter
  with pre-sized lists measured 64.6ms before and 64.7ms after at search
  depth four. No effect; the JIT had already dissolved the abstraction. The
  clearer stream version stays, and the negative result is recorded here so
  the experiment is not repeated.

## Make and unmake

MutablePosition is the engine's working state: moves are made on one
position and unmade on backtrack, with the Zobrist key maintained by XOR as
features change. Its correctness anchors to an oracle rather than caution:
make must produce exactly what Position.apply produces for every legal move
of stress positions, the incremental key must equal the recomputed one, and
the twenty perft counts must survive the switch. Getting the speed took
three measured rounds: the first wiring regressed every benchmark because
make paid for two set copies per move; int-bitmask rights made the path
allocation-free. Perft(3) improved from 3.53ms to 2.83ms; legalMoves stayed
at par because the per-call scratch copy offsets the per-candidate gain;
search awaited its own migration, which this infrastructure enabled.

The search migration followed and delivered the payoff: alpha-beta and
quiescence now walk one scratch copy of the root with make and unmake,
generate pseudo-legal moves and fold the legality test into the tree walk
(each candidate is made exactly once, serving both the check test and the
recursion), and feed the transposition table from the incrementally
maintained key instead of a sixty-four-square rescan per node. Depth-four
search fell from 65ms to 20ms, three and a quarter times faster, with every
other benchmark at par; the same half-second budget that reached depth four
before now completes depth five. The exhaustive reference search
deliberately stays on the immutable path, so the equivalence tests prove
the fast path against an oracle that cannot share its bugs.

## Null move pruning

The first speculative cut came with its own honest depth profile,
measured before the assertion was written: at depth three it never fires,
because the recursion never sees a node deep enough; at depth four it is
a wash, 55065 nodes to 55112, the failed null searches costing what the
rare cutoffs save; at depth five it removes 42 percent of the tree,
391843 nodes to 227991, with the same score and the same move. The
depth-four benchmark still improved from 19.6ms to 14.7ms because the
transposition table turns even shallow null searches into useful cached
bounds. A two-second think at the start position now reaches depth six on
half the nodes it needed before.

## Aspiration windows

Measured on the full playing configuration at depth six before shipping:
the start position spent 103842 nodes against 119165 without aspiration,
thirteen percent fewer; Kiwipete 696002 against 741740 and the fine rook
endgame 6529 against 6926, six percent fewer each; identical scores and
best moves throughout. A deliberately narrow window costs a full
re-search when the score swings, so the win is bounded and the loss is
rare; the deepening loop itself joined the benchmark suite as
deepeningDepthSix, baseline 256ms, so future search work is measured on
the loop real play uses rather than a single fixed depth.

## Killer moves and quiet history

The largest single search win so far, and it cost no speculation at all:
ordering never changes the answer, only how fast it arrives. Remembering
each ply's last two quiet refuters and crediting every quiet refuter in a
global history table collapsed the quiet-opening tree from 103842 nodes to
21230 at depth six, five times smaller, while the tactical Kiwipete
middlegame, already well ordered by victim and attacker, gave up eleven
percent and the fine endgame six. The benchmarks moved accordingly:
deepeningDepthSix from 256ms to 52ms, searchDepthFour from 15ms to 4.5ms,
identical scores and best moves throughout. The lesson mirrors the
iteration-ten one that introduced ordering in the first place: alpha-beta
is only as good as the first move it tries, and the cheapest nodes are the
ones never visited.

## The roadmap this motivates

With make and unmake now under both perft and search, the object model pays
for its clarity in exactly one place: the make-per-candidate legality
filter. The measured path forward, in order of expected yield per unit of
disruption:

1. Pin-aware legality: generate strictly legal moves directly and skip the
   filter for the majority of moves that provably cannot expose the king.
2. Bitboards: piece-centric sets with precomputed attack tables, the
   endgame of this progression and the reason Square.index has been
   bitboard-compatible since iteration two.

Each step lands only with before-and-after numbers from this suite.
