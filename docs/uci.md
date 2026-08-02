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
- `go infinite`, which searches until told otherwise, and `stop`, which
  ends the running search and waits for its bestmove.
- One `info` line per search with depth, score (`cp` or `mate`, converted
  from the internal ply distances), and nodes, then `bestmove`, with the
  null move `0000` answering positions where the game is over.

## The search session

Searching runs on a worker thread beside the protocol loop, because the
protocol demands it: a GUI pings `isready` while the engine thinks and
expects `readyok` back, and `go infinite` has no end except the `stop`
command, which must be heard while searching. The two threads share almost
nothing. The stop request is a single atomic flag, polled by the search at
every node; the position and limits are captured when the search starts,
and both are immutable values, so no later command can corrupt a running
search. `stop`, `quit`, and the end of input all set the flag and then wait
for the worker to finish, which is prompt because polling happens at every
node, and safe because iterative deepening guarantees the depth-one result
before honoring any signal: a bestmove always arrives.

One search runs at a time. A `go` while one is running is rejected with an
info string, which no correctly written GUI ever triggers: the protocol
obliges it to wait for bestmove or send stop first.

## Deliberate limitations

No options are advertised yet; hash size and search settings arrive with
the configuration work. Pondering, searching on the opponent's time, is
likewise future work.

## References

- The UCI protocol description:
  https://www.chessprogramming.org/UCI
