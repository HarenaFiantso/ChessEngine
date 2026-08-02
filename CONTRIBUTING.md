# Contributing to Saitama

Saitama is a learning vehicle first and a chess engine second. Every
feature exists to teach something, so contributions are judged as much by
their clarity as by their strength: a change the next reader cannot follow
is not done, however many Elo it wins.

## Getting started

You need nothing but a JVM capable of running Gradle; the build provisions
JDK 26 itself through Gradle toolchains.

```sh
./gradlew build         # compile everything and run every quality gate
./gradlew :engine:run   # talk UCI to the engine
./gradlew :gui:run      # play it on the built-in board
```

The repository is two modules. `engine` holds the rules, search,
evaluation, and UCI layers and depends on nothing graphical. `gui` holds
the JavaFX board and depends on the engine; that arrow never reverses.

## The quality gates

`./gradlew build` must pass before any commit. That single command runs:

- Spotless with google-java-format; run `./gradlew spotlessApply` before
  committing rather than fighting it
- Checkstyle with zero tolerated warnings
- Error Prone with warnings promoted to errors
- SpotBugs
- The full test suite with a 90 percent line-coverage floor, verified by
  JaCoCo

Every commit in history builds green on its own, not only the branch tip.
If a commit needs a follow-up fix, amend or squash before opening the
pull request.

## Commits and branches

- One iteration of work per branch, named by intent: `feat/...`,
  `perf/...`, `docs/...`, `build/...`.
- [Conventional Commits](https://www.conventionalcommits.org/), atomic and
  imperative: `feat(search): ...`, `perf(rules): ...`. One logical change
  per commit.
- No emoji anywhere: commits, code, comments, or documentation.
- Comments only where the code cannot speak for itself, and then as
  Javadoc.

## Testing expectations

Tests pin behavior, not implementations. The house style, worth reading
before adding to it:

- Rules changes answer to perft: the twenty exact node counts in
  PerftTest are the ground truth of move generation, and a new mechanic
  needs positions that would catch its absence.
- Fast paths answer to oracles: MutablePosition must produce exactly what
  the immutable Position produces, move for move.
- Search changes respect the exact-versus-speculative split. Exact
  techniques must keep AlphaBetaSearch score-identical to NegamaxSearch,
  proven in the equivalence suite on the configuration with speculation
  disabled. Speculative techniques (the null move, reductions, exchange
  pruning) live behind that toggle and are held to what they promise:
  fewer nodes, same mates, same best moves where it matters.

## Performance work

Optimization claims cite numbers or do not merge.

- `./gradlew jmh` runs the benchmark suite; before-and-after numbers
  belong in the pull request.
- Wall clock is the verdict, node counts are the diagnosis; report both.
- Negative results are results: a change that measures flat is reverted
  and recorded in `docs/performance.md` so nobody repeats the experiment.

## Documentation

Each feature lands with its explanation in `docs/`, written for a reader
meeting the concept for the first time: why it exists, what it trades,
and how it was validated. The documents there show the expected depth.

## License

Saitama is released under the GNU General Public License, version 3. By
contributing you agree that your contributions are licensed under the
same terms.
