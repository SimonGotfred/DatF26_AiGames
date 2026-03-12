package chess;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static chess.Piece.board;

public enum Type
{
    PAWN  ('♟',(xy) -> {return Set.of();}),
    BISHOP('♝',(xy) -> {return Set.of();}),
    KNIGHT('♞',(xy) -> {return Set.of();}),
    ROOK  ('♜',(xy) ->
    {
        List<char[]> moves = new java.util.ArrayList<>();

        for (int i = xy[0]+1; i < 8; i++)
        {
            moves.add(new char[]{(char)i , xy[1]});
            if (board.stream().anyMatch(piece -> Arrays.equals(piece.position, moves.getLast()))) break;
        }

        for (int i = xy[0]-1; i > -1; i--)
        {
            moves.add(new char[]{(char)i, xy[1]});
            if (board.stream().anyMatch(piece -> Arrays.equals(piece.position, moves.getLast()))) break;
        }

        for (int i = xy[1]+1; i < 8; i++)
        {
            moves.add(new char[]{xy[0], (char)i});
            if (board.stream().anyMatch(piece -> Arrays.equals(piece.position, moves.getLast()))) break;
        }

        for (int i = xy[1]-1; i > -1; i--)
        {
            moves.add(new char[]{xy[0], (char)i});
            if (board.stream().anyMatch(piece -> Arrays.equals(piece.position, moves.getLast()))) break;
        }

        return Set.copyOf(moves);
    }),

    QUEEN ('♛',(xy) ->
    {
        Set<char[]> moves = new java.util.HashSet<>();
        moves.addAll(BISHOP.movesFrom(xy));
        moves.addAll(ROOK.movesFrom(xy));
        return moves;
    }),

    KING  ('♚',(xy) ->
    {
        Set<char[]> moves = new java.util.HashSet<>();

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                moves.add(new char[]{((char) (xy[0] + i)), ((char) (xy[1] + j))});
            }
        }

        return moves;
    });

    static final   String white = "♚♛♜♝♞♟";
    static final   String black = "♔♕♖♗♘♙";
    static boolean contains(char c) {return c >= '♔' && c <= '♟';}

    final char icon;
    private final Function<char[],Set<char[]>> pattern;

    Type(char icon, Function<char[],Set<char[]>> pattern)
    {
        this.icon = icon;
        this.pattern = pattern;
    }

    public Set<char[]> movesFrom(String position) {return movesFrom(position.charAt(1),position.charAt(0));}
    public Set<char[]> movesFrom(char rank, char file) {return movesFrom(new char[]{rank,file});}
    public Set<char[]> movesFrom(char[] position)
    {
        Set<char[]> moves = pattern.apply(position);

        moves.remove(position);
        moves.removeIf(p
                        -> p[0] > 7
                        || p[1] > 7);

        return moves;
    }
}
