package chess;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Type
{
    PAWN  ('♟', 1,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();

        int d = piece.isWhite() ? -1:1;

        if (!piece.board.pieceAt(piece.x(), (char)(piece.y()+d)))
        {
            moves.add(new char[]{piece.x(), (char)(piece.y()+d)});
            if (piece.y() == 6 || piece.y() == 1 && !piece.board.pieceAt(piece.x(), (char)(piece.y()+d+d)))
                moves.add(new char[]{piece.x(), (char)(piece.y()+d+d)});
        }

        for (int i : new int[]{-1,1})
        {
            if (piece.board.at((char)(piece.x()+i), (char)(piece.y()+d))!=' ')
                moves.add(new char[]{(char)(piece.x()+i), (char)(piece.y()+d)});
        }

        return moves.stream();
    }), // todo: promotion & en passant

    KNIGHT('♞', 3,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();

        for (int i : new int[]{-1,1})
        {
            for (int j : new int[]{-2,2})
            {
                moves.add(new char[]{(char) (piece.x() + i), (char) (piece.y() + j)});
                moves.add(new char[]{(char) (piece.x() + j), (char) (piece.y() + i)});
            }
        }

        return moves.stream();
    }),

    BISHOP('♝', 3,(piece) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.x()+i) , (char)(piece.y()+i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.x()-i) , (char)(piece.y()-i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.x()+i) , (char)(piece.y()-i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.x()-i) , (char)(piece.y()+i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        return moves.stream();
    }),

    ROOK  ('♜', 5,(piece) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = piece.x()+1; i < 8; i++)
        {
            moves.add(new char[]{(char)i , piece.y()});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.x()-1; i > -1; i--)
        {
            moves.add(new char[]{(char)i, piece.y()});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.y()+1; i < 8; i++)
        {
            moves.add(new char[]{piece.x(), (char)i});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.y()-1; i > -1; i--)
        {
            moves.add(new char[]{piece.x(), (char)i});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        return moves.stream();
    }),

    QUEEN ('♛', 9,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();
        moves.addAll(BISHOP.movesFor(piece).toList());
        moves.addAll(ROOK.movesFor(piece).toList());
        return moves.stream();
    }),

    KING  ('♚', 100,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                moves.add(new char[]{((char) (piece.x() + i)), ((char) (piece.y() + j))});
            }
        }

        if (moves.contains(null)) System.out.println("what");

        return moves.stream().filter(pos -> !Arrays.equals(pos, piece.position));
    }), // todo: castling

    VACANT(' ', 0,(piece) -> Stream.empty());

    public static final   String white = "♚♛♜♝♞♟";
    public static final   String black = "♔♕♖♗♘♙";

    public static boolean isPiece(char c) {return c >= '♔' && c <= '♟';}
    public static boolean isWhite(char c) {return c >= '♚' && c <= '♟';}
    public static boolean isBlack(char c) {return c >= '♔' && c <= '♙';}

    public static int value(char c)
    {
        return switch (c)
        {
            case '♟' ->    1;
            case '♞',
                 '♝' ->    3;
            case '♜' ->    5;
            case '♛' ->    9;
            case '♚' ->  100;
            case '♙' ->   -1;
            case '♘',
                 '♗' ->   -3;
            case '♖' ->   -5;
            case '♕' ->   -9;
            case '♔' -> -100;
            default ->     0;
        };
    }

    public static Type fromChar(char c)
    {
        return switch (c)
        {
            case '♟', '♙' -> PAWN;
            case '♞', '♘' -> KNIGHT;
            case '♝', '♗' -> BISHOP;
            case '♜', '♖' -> ROOK;
            case '♛', '♕' -> QUEEN;
            case '♚', '♔' -> KING;
            default -> VACANT;
        };
    }

    public static char invert(char piece)
    {
        if      (Type.isWhite(piece)) return (char)(piece-6);
        else if (Type.isBlack(piece)) return (char)(piece+6);
        else                          return  ' ';
    }

    public static Piece invert(Piece piece)
    {
        return new Piece(invert(piece.icon()),piece.board,piece.position);
    }

    public  final char icon;
    public  final int value;
    private final Function<Piece,Stream<char[]>> pattern;

    Type(char icon, int value, Function<Piece,Stream<char[]>> pattern)
    {
        this.icon    = icon;
        this.value   = value;
        this.pattern = pattern;
    }

    public Stream<char[]> movesFor(Piece piece)
    {
        return pattern.apply(piece).filter(p -> p[0] < 8 && p[1] < 8);
    }
}
