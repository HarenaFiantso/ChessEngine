# ChessEngine

An educational chess engine written in modern Java, built incrementally with a
focus on software engineering excellence: clean architecture, exhaustive
testing, measured optimization, and a Git history that tells the story of every
design decision.

> [!NOTE]
> The build, toolchain, and test pipeline are in place.
> The engine itself does not exist yet... it will grow one reviewed, tested,
> documented iteration at a time.

## Goals

This project is a learning vehicle first and a chess engine second. Every
feature exists to teach something: board representation teaches data modeling
and bit manipulation, move generation teaches correctness discipline (perft),
search teaches algorithmic optimization, and the surrounding tooling teaches
professional build, test, and CI practices.

Playing strength matters, but never at the expense of readability,
correctness, or maintainability.

## Requirements

- Nothing but a JVM capable of running Gradle. The build uses
  [Gradle toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
  to compile and test against **Java 26**, auto-provisioning that JDK if it is
  not installed locally.

## Building and running

```sh
./gradlew build   # compile and run all checks
./gradlew test    # run the test suite
./gradlew run     # run the engine
```

## Roadmap

The engine is built strictly incrementally; each milestone is completed,
tested, documented, and committed before the next begins.

1. **Foundations**: build tooling, CI, code quality gates, documentation.
2. **Board model**: squares, pieces, colors, moves, FEN parsing and printing.
3. **Rules**: application and undo, attack detection, legal move
   generation, validated by perft test suites.
4. **Thinking**: evaluation, minimax, alpha-beta, iterative deepening, move
   ordering, quiescence search.
5. **Speed**: Zobrist hashing, transposition tables, pruning and reduction
   techniques, benchmarked with JMH.
6. **Playing**: UCI protocol, time management, opening book, configuration.

## Design principles

- Correctness before performance; performance only with benchmarks.
- Readability over cleverness.
- One logical change per commit, following
  [Conventional Commits](https://www.conventionalcommits.org/).
- Every non-obvious decision is documented, in code or in decision records.
