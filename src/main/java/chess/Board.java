package chess;

import java.util.*;
import java.util.stream.Stream;

public class Board
{
    private final char[][] board;

    public Board(char[][] board)
    {
        this.board = board;
    }

    public char    at     (char... pos) {return board[pos[0]][pos[1]];}
    public boolean whiteAt(char... pos) {return Type.isWhite(at(pos));}
    public boolean blackAt(char... pos) {return Type.isBlack(at(pos));}
    public boolean pieceAt(char... pos) {return Type.isPiece(at(pos));}

    public Set<Piece> whites()
    {
        Set<Piece> whites = new HashSet<>();
        int rank = 0;
        for (char[] s : board)
        {
            int file = 0;
            for (char c : s)
            {
                if (Type.isWhite(c)) whites.add(new Piece(c, rank, file));
                file++;
            }
            rank++;
        }
        return whites;
    }

    public Set<Piece> blacks()
    {
        Set<Piece> blacks = new HashSet<>();
        int rank = 0;
        for (char[] s : board)
        {
            int file = 0;
            for (char c : s)
            {
                if (Type.isBlack(c)) blacks.add(new Piece(c, rank, file));
                file++;
            }
            rank++;
        }
        return blacks;
    }

    public char[][] move(char[] from, char[] to)
    {
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = ' ';
        return board;
    }

    public HashMap<Stream<char[]>,Piece> legalMoves()
    {
        HashMap<Stream<char[]>,Piece> moves = new HashMap<>();
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
