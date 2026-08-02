# ChessEngine

An educational chess engine written in modern Java, built incrementally with a
focus on software engineering excellence: clean architecture, exhaustive
testing, measured optimization, and a Git history that tells the story of every
design decision.

> [!NOTE]
> The engine plays. It speaks UCI, so any chess GUI can load it, and it
> ships its own JavaFX board; see Playing against it below. Rules are
> validated by perft, search uses alpha-beta with quiescence, a
> transposition table, null move pruning, and iterative deepening with
> aspiration windows under time control.

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
./gradlew build         # compile and run all checks
./gradlew :engine:run   # talk UCI to the engine interactively
./gradlew :gui:run      # play against the engine on the built-in board
./gradlew installDist   # build a standalone launcher for third-party GUIs
```

## Playing against it

The quickest way is the built-in JavaFX board:

```sh
./gradlew :gui:run
```

You play white by clicking; Saitama answers as black. The window offers
legal-move highlighting, promotion choice, and a new-game button.

The engine also speaks the
[Universal Chess Interface](https://www.chessprogramming.org/UCI), the
protocol every chess GUI understands. Build the launcher and register it in
your GUI of choice (Arena, Cute Chess, Lucas Chess, BanksiaGUI, and others):

```sh
./gradlew installDist
# engine binary: engine/build/install/ChessEngine/bin/ChessEngine
```

Point the GUI at that script as a UCI engine and play. The engine honors
depth, fixed move time, and game-clock time controls.

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
