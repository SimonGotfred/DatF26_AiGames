package chess;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Type
{
    PAWN  ('♟', 1,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();

        if (!piece.board.pieceAt(piece.file(), (char)(piece.rank()+1)))
            moves.add(new char[]{piece.file(), (char)(piece.rank()+1)});

        for (int i : new int[]{-1,1})
        {
            if (piece.board.blackAt((char)(piece.file()+i), (char)(piece.rank()+1)))
                moves.add(new char[]{(char)(piece.file()+i), (char)(piece.rank()+1)});
        }

        return moves.stream();
    }), // todo: promotion & en passant

    BISHOP('♝', 3,(piece) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.position[1]+i) , (char)(piece.position[1]+i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.position[1]-i) , (char)(piece.position[1]-i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.position[1]+i) , (char)(piece.position[1]-i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new char[]{(char)(piece.position[1]-i) , (char)(piece.position[1]+i)});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        return moves.stream();
    }),

    KNIGHT('♞', 3,(piece) ->
    {
        Set<char[]> moves = new HashSet<>();

        for (int i : new int[]{-1,1})
        {
            for (int j : new int[]{-2,2})
            {
                moves.add(new char[]{(char) (piece.position[0] + i), (char) (piece.position[1] + j)});
                moves.add(new char[]{(char) (piece.position[0] + j), (char) (piece.position[1] + i)});
            }
        }

        return moves.stream();
    }),

    ROOK  ('♜', 4,(piece) ->
    {
        List<char[]> moves = new ArrayList<>();

        for (int i = piece.position[0]+1; i < 8; i++)
        {
            moves.add(new char[]{(char)i , piece.position[1]});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.position[0]-1; i > -1; i--)
        {
            moves.add(new char[]{(char)i, piece.position[1]});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.position[1]+1; i < 8; i++)
        {
            moves.add(new char[]{piece.position[0], (char)i});
            if (piece.board.pieceAt(moves.getLast()))
            {if (piece.board.whiteAt(moves.getLast())) moves.removeLast();break;}
        }

        for (int i = piece.position[1]-1; i > -1; i--)
        {
            moves.add(new char[]{piece.position[0], (char)i});
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
                moves.add(new char[]{((char) (piece.position[0] + i)), ((char) (piece.position[1] + j))});
            }
        }

        return moves.stream().filter(pos -> Arrays.equals(pos, piece.position));
    }); // todo: castling

    static final   String white = "♚♛♜♝♞♟";
    static final   String black = "♔♕♖♗♘♙";

    static boolean isPiece(char c) {return c >= '♔' && c <= '♟';}
    static boolean isWhite(char c) {return c >= '♚' && c <= '♟';}
    static boolean isBlack(char c) {return c >= '♔' && c <= '♙';}

    static int value(char c)
    {
        return switch (c)
        {
            case '♟' -> 1;
            case '♞',
                 '♝' -> 3;
            case '♜' -> 4;
            case '♛' -> 9;
            case '♚' -> 100;
            case '♙' -> -1;
            case '♘',
                 '♗' -> -3;
            case '♖' -> -4;
            case '♕' -> -9;
            case '♔' -> -100;
            default -> 0;
        };
    }

    final char icon;
    final int value;
    private final Function<Piece,Stream<char[]>> pattern;

    Type(char icon, int value, Function<Piece,Stream<char[]>> pattern)
    {
        this.icon    = icon;
        this.value   = value;
        this.pattern = pattern;
    }

    public Stream<char[]> movesFor(Piece piece)
    {
        Stream<char[]> moves = pattern.apply(piece);
        return moves.filter(p -> p[0] < 8 && p[1] < 8);
    }
}
