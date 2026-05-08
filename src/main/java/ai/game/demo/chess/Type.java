package ai.game.demo.chess;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static ai.game.demo.chess.Board.Position;

public enum Type
{
    PAWN  ('♟', 1,(position) ->
    {
        Set<char[]> moves = new HashSet<>();

        int d = position.whiteAt() ? -1:1; // check *alleged* pawn color for move direction
        //double move
        if (!position.board().pieceAt(position.x(), (char)(position.y()+d)))
        {
            moves.add(new char[]{position.x(), (char)(position.y()+d)});
            if (position.y() == 6 || position.y() == 1 && !position.board().pieceAt(position.x(), (char)(position.y()+d+d)))
                moves.add(new char[]{position.x(), (char)(position.y()+d+d)});
        }

        for (int i : new int[]{-1,1})
        {
            if (position.board().at((char)(position.x()+i), (char)(position.y()+d))!='ㅤ')
                moves.add(new char[]{(char)(position.x()+i), (char)(position.y()+d)});
        }

        //en passant


        return moves.stream();
    }), // todo: promotion & en passant

    KNIGHT('♞', 3,(position) ->
    {
        Set<char[]> moves = new HashSet<>();

        for (int i : new int[]{-1,1})
        {
            for (int j : new int[]{-2,2})
            {
                moves.add(new char[]{(char) (position.x() + i), (char) (position.y() + j)});
                moves.add(new char[]{(char) (position.x() + j), (char) (position.y() + i)});
            }
        }

        return moves.stream();
    }),

    BISHOP('♝', 3,(position) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(position.x()+i) , (char)(position.y()+i)});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(position.x()-i) , (char)(position.y()-i)});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(position.x()+i) , (char)(position.y()-i)});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(position.x()-i) , (char)(position.y()+i)});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        return moves.stream();
    }),

    ROOK  ('♜', 5,(position) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = position.x()+1; i < 8; i++)
        {
            moves.add(new char[]{(char)i , position.y()});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = position.x()-1; i > -1; i--)
        {
            moves.add(new char[]{(char)i, position.y()});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = position.y()+1; i < 8; i++)
        {
            moves.add(new char[]{position.x(), (char)i});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = position.y()-1; i > -1; i--)
        {
            moves.add(new char[]{position.x(), (char)i});
            if (position.board().pieceAt(moves.getLast())) break;
//            {if (position.board().whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        return moves.stream();
    }),

    QUEEN ('♛', 9,(position) ->
    {
        Set<char[]> moves = new HashSet<>();
        moves.addAll(BISHOP.movesFrom(position).toList());
        moves.addAll(ROOK.movesFrom(position).toList());
        return moves.stream();
    }),

    KING  ('♚', 100,(position) ->
    {
        Set<char[]> moves = new HashSet<>();

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                moves.add(new char[]{((char) (position.x() + i)), ((char) (position.y() + j))});
            }
        }

        if (moves.contains(null)) System.out.println("what");

        return moves.stream().filter(pos -> !Arrays.equals(pos, position.position()));
    }), // todo: castling

    VACANT(' ', 0,(position) -> Stream.empty());

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
        return new Piece(invert(piece.icon()),piece.position);
    }

    public  final char icon;
    public  final int value;
    private final Function<Position,Stream<char[]>> pattern;

    Type(char icon, int value, Function<Position,Stream<char[]>> pattern)
    {
        this.icon    = icon;
        this.value   = value;
        this.pattern = pattern;
    }

    public Stream<char[]> movesFrom(Position position)
    {
        return pattern.apply(position).filter(p -> p[0] < 8 && p[1] < 8);
    }
}
