# Evaluation

How the engine turns a position into a number search can compare.

## Conventions

Scores are centipawns, one hundredth of a pawn. Every evaluator reports from
the side to move's point of view: positive means the mover stands better.
That orientation is the negamax convention; search negates a child's score
instead of tracking whose turn it is, and the sign-flip property (the same
board scored for the other side negates) is pinned by tests because negamax
silently depends on it.

`Evaluator` is a deliberate Strategy seam. Search receives one through its
constructor, so evaluation policy improves independently of the algorithms
consuming it, and tests can plug in trivial evaluators to isolate search
behavior.

## Terms implemented

- Material: pawn 100, knight 320, bishop 330, rook 500, queen 900. Kings
  carry no value since both are always present.
- Piece-square tables, Michniewski's simplified set: per-square bonuses
  encoding positional lore such as centralized knights, advanced central
  pawns, and kings sheltered in the corner. Tables are transcribed visually
  with rank 8 first, which is exactly a black piece's view of the board;
  white lookups mirror vertically by XOR-ing the square index with 56, a
  direct payoff of the little-endian rank-file contract.

## Deliberately missing

Tapered evaluation (interpolating middlegame and endgame tables as material
leaves the board), pawn structure, mobility, and king safety all await the
benchmark and self-play infrastructure that can prove their worth. The king
table in particular is middlegame-only and actively wrong for endgames, where
the king should centralize; that known deficiency is the motivating example
for tapered evaluation later.
