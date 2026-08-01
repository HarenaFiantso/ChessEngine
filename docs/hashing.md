# Zobrist Hashing, Repetition, and the Transposition Table

One mechanism, position identity in 64 bits, serves three customers: the
threefold repetition rule, the transposition table, and eventually the UCI
game history.

## Zobrist keys

Every board feature owns a fixed random 64-bit number: each piece on each
square, black to move, each castling right, each en passant file. A
position's key is the exclusive-or of its present features. Two properties
make this scheme the industry standard:

- Exclusive-or is self-inverse, so when make/unmake arrives a move will
  update the key by XOR-ing out the vanished features and XOR-ing in the new
  ones, instead of the full-board scan performed today.
- The move clocks are excluded on purpose. The repetition rule treats
  positions as equal regardless of clocks, so a knight shuffle returning to
  the starting position hashes identically to it even though the FEN records
  differ, which is precisely what repetition detection needs.

Distinct positions can collide in principle; every consumer accepts key
equality as position identity. With 64-bit keys the collision probability is
negligible against the number of positions a search visits, and this trade is
universal among engines.

## The game container and threefold repetition

`Game` pairs the current position with the keys of every position reached.
The repetition draw is claimed when the current key has occurred three
times. Decisive verdicts outrank it, matching FIDE: a move that mates ends
the game even if it also repeats. `Game.play` accepts only legal moves,
giving the future UCI layer a strict boundary.

## The transposition table

Different move orders reach identical positions constantly; the table lets
the second arrival reuse the first one's work. Entries live in a fixed-size
power-of-two array indexed by the key's low bits, newest entry winning the
slot, full key verified on probe because many keys share a slot.

Three subtleties carry most of the correctness weight:

- Bounds. Alpha-beta windows leave most nodes knowing only that their value
  lies above beta (a lower bound) or below alpha (an upper bound). Entries
  record which, and probes only use a bound when it actually clears the
  current window. Treating a bound as exact corrupts the search silently.
- Mate distances. A mate score encodes distance from the root, but the same
  position can be reached at different plies. Scores near the mate range are
  translated to node-relative distances on store and back on probe; the mate
  tests run against the caching configuration to pin this.
- The remembered best move is tried first on revisits, the strongest move
  ordering signal available, often producing an immediate cutoff.

A known wrinkle is accepted and documented: keys ignore the halfmove clock,
so a cached score cannot see an impending fifty-move draw. Engines live with
this at search level and adjudicate the rule at the game level.

Because a table entry may carry information from a deeper subtree, a cached
search can legitimately return a different fixed-depth score than the
exhaustive reference; pruning-soundness tests therefore run the
disabled-table configuration, while separate tests pin the table's node
savings and mate exactness.

## References

- Chess Programming Wiki, Zobrist Hashing and Transposition Table:
  https://www.chessprogramming.org/Zobrist_Hashing
  https://www.chessprogramming.org/Transposition_Table
