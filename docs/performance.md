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
| MoveGenerator.legalMoves (Kiwipete) | 15.2 us |
| Perft(3) from the start | 2.8 ms |
| Alpha-beta depth 4, fresh table | 65 ms |

Legal move generation dominates everything built on it. Its cost is the
legality filter: every pseudo-legal candidate is applied (a board copy) and
answered by isInCheck (a reverse attack scan). Roughly fifty candidates in a
middlegame means legalMoves costs about fifty apply-and-check round trips.

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
search awaits its own migration, which this infrastructure now enables.

## The roadmap this motivates

The object model pays for its clarity in exactly one place: the
apply-per-candidate legality filter. The measured path forward, in order of
expected yield per unit of disruption:

1. Make/unmake with incremental Zobrist keys: mutate one board, undo on
   backtrack, update the hash by XOR instead of rescanning.
2. Pin-aware legality: generate strictly legal moves directly and skip the
   filter for the majority of moves that provably cannot expose the king.
3. Bitboards: piece-centric sets with precomputed attack tables, the
   endgame of this progression and the reason Square.index has been
   bitboard-compatible since iteration two.

Each step lands only with before-and-after numbers from this suite.
