# The JavaFX Board

How the built-in graphical interface is put together, and why the engine
does not know it exists.

## A second module, not a feature of the engine

Chess engines never link against their interfaces; iteration thirteen made
that point with UCI, and the built-in board does not weaken it. The
repository is now two Gradle modules. The engine module is exactly what it
was: rules, search, evaluation, UCI, no interface code, no JavaFX
dependency. The gui module depends on the engine and the dependency arrow
never reverses, the same relationship Arena or Cute Chess has to Stockfish,
except both sides happen to live in one repository. Deleting the gui module
would leave the engine untouched and still playable in every third-party
interface.

Both modules pass the same quality gates: the formatter, Checkstyle, Error
Prone, SpotBugs, and the coverage verification. The window classes
themselves are excluded from the coverage gate, because exercising a JavaFX
scene graph requires a display; everything with decision logic in it lives
outside those classes precisely so it stays testable.

## The session is the brain, the window is a shell

Click-to-move sounds trivial and is not: a click can select a piece,
reselect another, clear a selection, complete a move, or open a promotion
choice, and none of those may ever produce an illegal position. All of that
lives in GameSession, a class with no JavaFX import, driven entirely by
plain unit tests: click a square, assert what happened. The session answers
every click with what it did (selected, cleared, played, promoting,
ignored) and exposes the selected square and its legal targets for
highlighting. Moves reach the game through the same validating Game
container the UCI layer uses, so the interface cannot corrupt a position
any more than a hostile UCI script can.

The window itself, SaitamaGui plus BoardView, only paints session state
and forwards clicks. The board is drawn with the Unicode chess glyphs, so
there are no image assets to license or load.

## Thinking without freezing

The human plays white; when it is black's turn the window hands the
current position to the engine on one background thread and repaints when
the answer returns via the JavaFX event thread. Two details carry the
correctness. The position handed over is an immutable value, so the search
cannot observe the interface. And every new game increments a generation
counter whose value is captured when a search starts: a result returning
for a stale generation is discarded, so an engine move from an abandoned
game can never land on a fresh board.

The engine object is the same iterative deepening search real play uses,
kept for the whole session so its transposition table stays warm from move
to move, with a fixed budget under a second per move.

## Running it

`./gradlew :gui:run` opens the window. The engine remains a plain UCI
binary for every other interface; see the README's playing section.
