# Game Status

How the engine decides that a game is over, and why some verdicts wait.

## Decisive verdicts are corollaries

Once legal move generation exists, checkmate and stalemate stop being
algorithms and become definitions: a side with no legal moves has lost if its
king is attacked and drawn the game otherwise. `GameStatus.of` is a direct
transcription of that sentence. All the difficulty lives in the move
generator, where it was already paid for and perft-audited.

## Draw adjudication

- Fifty-move rule: drawn once the halfmove clock, maintained by move
  application since the position type landed, reaches one hundred halfmoves.
  Checkmate takes precedence on the very move that reaches the limit,
  matching FIDE's rule that mate ends the game first.
- Insufficient material: the engine adjudicates the standard dead-material
  set: bare kings, king and a single minor piece, or bishops only that all
  stand on squares of one shade. Two knights against a bare king remain
  formally sufficient because mating positions exist, even though none can be
  forced; general dead-position detection requires retrograde reasoning far
  beyond material counting, which is why every practical engine uses this
  same subset.
- Threefold repetition is deliberately missing. A position cannot know how
  often it has occurred; that requires game history. Repetition detection
  arrives together with the game container, and Zobrist hashing will make the
  membership test cheap.

## References

- FIDE Laws of Chess, articles 5 and 9.
- Chess Programming Wiki, Draw:
  https://www.chessprogramming.org/Draw
