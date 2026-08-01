# Board Model

This document explains the design of the `org.saitama.board` package: what a
board representation must do, the options engines choose from, and why this
project starts where it does.

## The representation problem

Every part of an engine asks the board questions, and different parts ask in
different directions:

- Square-centric questions: "what piece stands on e4?" Needed by FEN parsing,
  printing, move application, and the UCI protocol.
- Piece-centric questions: "where are all white knights?" Needed constantly by
  move generation, attack detection, and evaluation, the code that dominates
  engine runtime.

The two classic families of representations each favor one direction:

| Family | Idea | Strengths | Weaknesses |
|---|---|---|---|
| Mailbox | An array of 64 (or padded 10x12 / 0x88) cells, each holding a piece or empty | Trivially answers "what is on square X", simple, easy to debug | Piece-centric queries require scanning; move generation is loop-heavy and branchy |
| Bitboards | One 64-bit word per piece kind, one bit per square | Set operations become single CPU instructions (AND, OR, shifts, popcount); sliding attacks become table lookups | Harder to read and debug; correctness mistakes are silent bit garbage |

Serious engines (Stockfish among them) use bitboards for speed, usually with a
small mailbox array alongside for square-centric lookups.

## What this project does, and in which order

We begin with a type-safe object vocabulary, not with either optimization:
enums for `Color`, `File`, `Rank`, `Square`, `PieceType`, and `Piece`. The
charter demands correctness before performance, and nothing about early
correctness work (FEN, legality, perft) is bottlenecked by representation.
When search arrives and profiling shows where time goes, bitboards will be
introduced behind this same vocabulary and measured with JMH benchmarks.

That transition is prepared today through one deliberate contract:

## The little-endian rank-file contract

`Square.index()` numbers the board rank by rank from white's corner:

```
8 | 56 57 58 59 60 61 62 63
7 | 48 49 50 51 52 53 54 55
6 | 40 41 42 43 44 45 46 47
5 | 32 33 34 35 36 37 38 39
4 | 24 25 26 27 28 29 30 31
3 | 16 17 18 19 20 21 22 23
2 |  8  9 10 11 12 13 14 15
1 |  0  1  2  3  4  5  6  7
  +------------------------
     a  b  c  d  e  f  g  h
```

so `index = 8 * rank + file`. This is the little-endian rank-file (LERF)
mapping used by most bitboard engines: square index equals bit position in a
64-bit word, north is `<< 8`, east is `<< 1`. Choosing LERF now means the
future bitboard migration changes implementations, not meanings.

The mapping depends on enum declaration order, which Error Prone rightly
warns about (`EnumOrdinal`): reordering constants would silently renumber the
board. The dependency is therefore treated as a documented invariant, pinned
by boundary and round-trip tests over all 64 squares, and confined to a
single suppressed `index()` accessor per type that all other code funnels
through.

## Why enums everywhere

- Flyweight instances: there is exactly one `E4` and one `WHITE_KNIGHT`.
  Reference comparison is valid, placing a piece allocates nothing, and
  incorrect states such as a ninth file are unrepresentable.
- Exhaustive switches: when move generation switches over `PieceType`, adding
  a variant (or forgetting one) becomes a compile-time event.
- `EnumSet` and `EnumMap` are backed by arrays and bit words. A
  `EnumSet<Square>` is literally a 64-bit bitboard under the hood, which is a
  useful preview of where the design is heading.
- `File` and `Rank` as distinct types make transposed coordinates, a classic
  chess bug that plain ints invite, a compile error.

## Piece identity and FEN symbols

`Piece` couples `Color` and `PieceType` into twelve constants and owns the
FEN symbol convention (uppercase white, lowercase black: `'N'` is a white
knight, `'q'` a black queen). The symbol lives on `Piece` because it is piece
identity, not a formatting concern: FEN parsing, board printing, and later
debugging output all consume the same single definition.

## References

- Chess Programming Wiki, Board Representation:
  https://www.chessprogramming.org/Board_Representation
- Chess Programming Wiki, Square Mapping Considerations:
  https://www.chessprogramming.org/Square_Mapping_Considerations
- Chess Programming Wiki, Bitboards:
  https://www.chessprogramming.org/Bitboards
