package chess;

import java.util.*;
import java.util.stream.Stream;

public class Board implements Comparable<Board>
{
    private final char[][] board;
    public  final boolean inverted;

    public Board parent;
    public Set<Board> children;

    public Board(char[][] board, boolean inverted) {this.board = board;this.inverted = inverted;}
    public Board(char[][] board) {inverted = false; this.board = board;}
    public Board(String[] board)
    {
        inverted = false;
        this.board = new char[board.length][];
        for (int i = 0; i < board.length; i++) this.board[i] = board[i].toCharArray();
    }

    public Piece   getPiece(char... pos) {return new Piece(at(pos), this, (int)pos[1], (int)pos[0]);}
    public boolean whiteAt (char... pos) {return Type.isWhite(at(pos));}
    public boolean blackAt (char... pos) {return Type.isBlack(at(pos));}
    public boolean pieceAt (char... pos) {return Type.isPiece(at(pos));}
    public char    at      (char... pos)
    {
        try   {return board[pos[1]][pos[0]];}
        catch (ArrayIndexOutOfBoundsException _) {return ' ';}
    }

    public Set<Piece> whites()
    {
        Set<Piece> whites = new HashSet<>();
        int rank = 0;
        for (char[] s : board)
        {
            int file = 0;
            for (char c : s)
            {
                if (Type.isWhite(c)) whites.add(new Piece(c, this, rank, file));
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
                if (Type.isBlack(c)) blacks.add(new Piece(c, this, rank, file));
                file++;
            }
            rank++;
        }
        return blacks;
    }

    public int score()
    {
        int buffer = 0;
        for (char[] row : board)
        {
            for (char piece : row)
            {
                buffer += Type.value(piece);
            }
        }
        return buffer;
    }

    public Board invert() {return new Board(invert(board),!inverted);}
    public static char[][] invert(char[][] board)
    {
        char[][] inverted = new char[8][8];
        int i = 8, j;
        for (char[] row : board)
        {
            i--; j = 8;
            for (char piece : row)
            {
                j--; if (Type.isWhite(piece)) inverted[i][j] = (char) (piece-6);
                else if (Type.isBlack(piece)) inverted[i][j] = (char) (piece+6);
                else                          inverted[i][j] = ' ';
            }
        }
        return inverted;
    }

    public Board move(String move)
    {
        return new Board(move(move.split(",")[0].trim(), move.split(",")[1].trim()),inverted);
    }
    public char[][] move(String from, String to) {return move(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public char[][] move(char[] from, char[] to)
    {
        if (pieceAt(to)) announceCapture(getPiece(from),getPiece(to));
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        board[to[1]][to[0]] = board[from[1]][from[0]];
        board[from[1]][from[0]] = ' ';
        return board;
    }

    public Set<Board> explore() // todo: tactic
    {
        children = new HashSet<>();
        HashMap<Stream<char[]>,Piece> legal = legalMoves();
        for (Stream<char[]> moveSet : legal.keySet())
        {
            moveSet.forEach(move -> children.add(new Board(move(move,legal.get(moveSet).position))));
        }
        children.removeIf(b -> b.equals(parent));
        return children;
    }

    public HashMap<Stream<char[]>,Piece> legalMoves()
    {
        HashMap<Stream<char[]>,Piece> moves = new HashMap<>();
        whites().forEach(piece -> moves.put(piece.moves(), piece));
        return moves;
    }

    public boolean isLegalMove(String move) {return move.split(",").length == 2 && isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public boolean isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public boolean isLegalMove(char[] from, char[] to)
    {
        List<char[]> moves = getPiece(from).moves().toList();

        if (moves.stream().filter(p -> !whiteAt(p)).anyMatch(m -> Arrays.equals(m,to))) return true;
        System.out.print("\033[31;1;4mIllegal move: " + letterize(from,to) + " - ");
        if (moves.stream().anyMatch(m -> Arrays.equals(m,to))) System.out.print("cannot capture own piece.");
        else System.out.print("no path.");
        System.out.println("\033[0m");

        return false; // "\nPlease enter \"from , to\" as eg. \"a1 , b2\""
    }

    public static char[] normalize(char[] pos)
    {
        if(pos[0]>7) pos[0] -= 'a';
        if(pos[1]>7) pos[1] -= '1';
        pos[1] = (char)(7-pos[1]);
        return pos;
    }

    public static String letterize(char[] pos)
    {
        if(pos[0]<8) pos[0] += 'a';
        pos[1] = (char)(7-pos[1]);
        if(pos[1]<8) pos[1] += '1';
        return ""+pos[0]+pos[1];
    }

    public String letterize(char[] from, char[] to)
    {

        return new Piece(at(from), this, (int)from[1], (int)from[0]).type.name() + ' ' + letterize(from)+" to "+letterize(to);
    }

    public void announceCapture(Piece taker, Piece taken)
    {
        System.out.println("\033[33;3m" + taker.color() + " " + taker.name() + " captures " + taken.color() + " " + taken.name() + "\033[0m");
    }

    public String toString()
    {
        StringJoiner joiner = new StringJoiner("");
        String square = "░";

        joiner.add("    0   1   2   3   4   5   6   7\n");

        for (int i = 0; i < board.length; i++)
        {
            joiner.add((board.length-i)+" ");
            for (int j = 0; j < board[i].length; j++)
            {
                if ((i+j) % 2 != 0) square = "░░";
                else                square = "      ";

                if (Type.isPiece(board[i][j]))
                {
                    if (!inverted) joiner.add(" " + board[i][j]);
                    else           joiner.add(" " + Type.inverted(board[i][j]));
                }
                else joiner.add(square);
            }
            joiner.add(" "+i+"\n");
        }

        joiner.add("    a   b   c   d   e   f   g   h");

        return joiner.toString();
    }

//    public String toString()
//    {
//        StringJoiner joiner = new StringJoiner("\n");
//        for (char[] s : board) joiner.add(String.valueOf(s));
//        return joiner.toString();
//    }

    @Override
    public int compareTo(Board other) {return this.score() - other.score();}
}
