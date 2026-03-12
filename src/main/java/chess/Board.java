package chess;

import java.util.*;
import java.util.stream.Stream;

public class Board extends HashSet<Piece>
{
    private final char[][] board;

    public Board(char[][] board)
    {
        this.board = board;
        int rank = 0;
        for (char[] s : board)
        {
            int file = 0;
            for (char c : s)
            {
                if (Type.contains(c)) this.add(new Piece(c,rank,file));
                file++;
            }
            rank++;
        }
    }

    public Stream<Piece> whites(){return this.stream().filter(piece ->  piece.color);}
    public Stream<Piece> blacks(){return this.stream().filter(piece -> !piece.color);}

    public char[][] move(char[] from, char[] to)
    {
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = ' ';
        return board;
    }

    public HashMap<Set<char[]>,Piece> legalMoves()
    {
        HashMap<Set<char[]>,Piece> moves = new HashMap<>();
        whites().forEach(piece -> moves.put(piece.moves(), piece));
        return moves;
    }

    public String toString()
    {
        StringJoiner joiner = new StringJoiner("\n");
        for (char[] s : board) joiner.add(String.valueOf(s));
        return joiner.toString();
    }
}
