# The UCI Layer

How the engine talks to the outside world.

## Why UCI

Chess engines never link against graphical interfaces. The Universal Chess
Interface splits the work: the GUI owns the game, the clock, and the board
on screen; the engine owns the thinking; they exchange plain text lines over
standard input and output. Implementing the protocol once makes the engine
usable in every GUI, on Lichess through bridge tools, and in scripted
matches against other engines, with zero interface code in the engine.

## What is implemented

- `uci`, `isready`, `ucinewgame`, `quit`, and the protocol rule that unknown
  commands are ignored.
- `position startpos | fen <record> [moves ...]`, with moves in coordinate
  notation. Parsing recovers each move's kind from the position, and the
  Game container validates full legality, so a corrupt history is rejected
  with an info string rather than corrupting the engine's state.
- `go depth N | movetime T | wtime/btime/winc/binc [movestogo M]`. Clock
  fields flow through the time allocator: remaining time divided by assumed
  moves left, plus half the increment, capped at half the clock. A bare or
  unsupported go falls back to a default two-second budget.
- One `info` line per search with depth, score (`cp` or `mate`, converted
  from the internal ply distances), and nodes, then `bestmove`, with the
  null move `0000` answering positions where the game is over.

## Deliberate limitations

Searching is synchronous: the engine reads the next command only after
bestmove. `go infinite` therefore gets a budget instead of searching until
`stop`, and `stop` itself is not yet honored mid-search. Honoring them
requires a search thread beside the protocol thread; that concurrency step
deserves its own iteration. No options are advertised yet; hash size and
search settings arrive with the configuration work.

## References

- The UCI protocol description:
  https://www.chessprogramming.org/UCI
