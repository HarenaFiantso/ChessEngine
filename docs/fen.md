# Forsyth-Edwards Notation

Forsyth-Edwards Notation (FEN) encodes a complete chess position as a single
line of text. It is the lingua franca of the chess software world: engines
receive positions from GUIs through it (`position fen ...` in UCI), test
suites express positions with it, and databases index by it. This project
uses FEN both as an interchange format and as the backbone of its own tests,
because a position that can be written as one short string makes test cases
readable.

## The six fields

A full FEN record has six space-separated fields:

```
rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
|_______________________________________| | |__| | | |
                piece placement            |  |   | | fullmove number
                                 side to move |   | halfmove clock
                               castling rights    en passant target
```

1. Piece placement, described below.
2. Side to move: `w` or `b`.
3. Castling availability: `K`, `Q`, `k`, `q` for the sides that may still
   castle kingside or queenside, or `-` for none.
4. En passant target square, or `-`.
5. Halfmove clock: moves since the last capture or pawn advance, for the
   fifty-move rule.
6. Fullmove number, starting at 1 and incremented after black moves.

The board model currently implements field one; the other five belong to game
state and arrive with the position type, which will wrap a board together
with them.

## The placement grammar

Ranks are listed from 8 down to 1, separated by `/`, mirroring how a diagram
is read from white's side of the table. Within a rank, a letter places a
piece (uppercase white, lowercase black, using the English initials
`PNBRQK`) and a digit 1 through 8 skips that many empty squares.

`Fen.parsePlacement` is strict:

- exactly eight rank fields;
- each rank must account for exactly eight squares, no more, no fewer;
- adjacent digits (such as `44` for four plus four) are rejected, because
  tolerating them would give one position several encodings.

Strictness buys a guarantee: parsing and writing are exact inverses, so
`writePlacement(parsePlacement(s))` reproduces `s` byte for byte. Round-trip
identity is the property the test suite leans on, and it only holds when the
accepted language is unambiguous.

## References

- Chess Programming Wiki, Forsyth-Edwards Notation:
  https://www.chessprogramming.org/Forsyth-Edwards_Notation
- The PGN standard, section 16.1 (FEN):
  https://www.chessclub.com/help/PGN-spec
