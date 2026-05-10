package ai.game.demo.chess;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public enum Type
{
    PAWN  ('♟', Color.WHITE, 100,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();

        int d = board.whiteAt(position) ? -1:1; // check *alleged* pawn color for move direction
        if (!board.pieceAt(position[0], position[1]+d))
        {
            moves.add(new int[]{position[0], position[1]+d});

            //double move
            if ((position[1] == 6 || position[1] == 1) && !board.pieceAt(position[0], position[1]+d+d))
                moves.add(new int[]{position[0], position[1]+d+d});
        }

        for (int i : mirror())
        {
            int[] checkedPos = new int[]{position[0]+i, position[1]+d};
            if (board.at(checkedPos).icon!='ㅤ' || board.passantAt(checkedPos[0],d<0?2:5))
                moves.add(checkedPos);
        }

        //en passant


        return moves.stream();
    },
    new int[][] // boardWorth
    {
        { 30, 30, 30, 30, 30, 30, 30, 30}, // 1
        { 50, 50, 50, 50, 50, 50, 50, 50}, // 2
        { 10, 10, 20, 30, 30, 20, 10, 10}, // 3
        {  5,  5, 10, 25, 25, 10,  5,  5}, // 4
        {  0,  0,  0, 20, 20,  0,  0,  0}, // 5
        {  5, -5,-10,  0,  0,-10, -5,  5}, // 6
        {  5, 10, 10,-20,-20, 10, 10,  5}, // 7
        {  0,  0,  0,  0,  0,  0,  0,  0}  // 8
    }), // todo: promotion & en passant

    KNIGHT('♞', Color.WHITE, 320,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();

        for (int i : mirror())
        {
            for (int j : mirror2())
            {
                moves.add(new int[]{position[0] + i, position[1] + j});
                moves.add(new int[]{position[0] + j, position[1] + i});
            }
        }

        return moves.stream();
    },
    new int[][] // boardWorth
    {
        {-50,-40,-30,-30,-30,-30,-40,-50}, // 1
        {-40,-20,  0,  0,  0,  0,-20,-40}, // 2
        {-30,  5, 10, 15, 15, 10,  5,-30}, // 3
        {-30,  0, 15, 20, 20, 15,  0,-30}, // 4
        {-30,  0, 15, 20, 20, 15,  0,-30}, // 5
        {-30,  5, 10, 15, 15, 10,  5,-30}, // 6
        {-40,-20,  0,  5,  5,  0,-20,-40}, // 7
        {-50,-40,-30,-30,-30,-30,-40,-50}  // 8
    }),

    BISHOP('♝', Color.WHITE, 330,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(new int[]{position[0]+i , position[1]+i});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new int[]{position[0]-i , position[1]-i});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new int[]{position[0]+i , position[1]-i});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(new int[]{position[0]-i , position[1]+i});
            if (board.pieceAt(moves.getLast())) break;
        }

        return moves.stream();
    },
    new int[][] // boardWorth
    {
        {-20,-10,-10,-10,-10,-10,-10,-20}, // 1
        {-10,  0,  0,  0,  0,  0,  0,-10}, // 2
        {-10,  0,  5, 10, 10,  5,  0,-10}, // 3
        {-10,  5,  5, 10, 10,  5,  5,-10}, // 4
        {-10,  0, 10, 10, 10, 10,  0,-10}, // 5
        {-10, 10, 10, 10, 10, 10, 10,-10}, // 6
        {-10,  5,  0,  0,  0,  0,  5,-10}, // 7
        {-20,-10,-10,-10,-10,-10,-10,-20}  // 8
    }),

    ROOK  ('♜', Color.WHITE, 500,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();

        for (int i = position[0]+1; i < 8; i++)
        {
            moves.add(new int[]{i , position[1]});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = position[0]-1; i > -1; i--)
        {
            moves.add(new int[]{i, position[1]});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = position[1]+1; i < 8; i++)
        {
            moves.add(new int[]{position[0], i});
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = position[1]-1; i > -1; i--)
        {
            moves.add(new int[]{position[0], i});
            if (board.pieceAt(moves.getLast())) break;
        }

        return moves.stream();
    },
    new int[][] // boardWorth
    {
        {  0,  0,  0,  0,  0,  0,  0,  0}, // 1
        {  5, 10, 10, 10, 10, 10, 10,  5}, // 2
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 3
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 4
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 5
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 6
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 7
        {  0,  0,  0,  5,  5,  0,  0,  0}  // 8
    }),

    QUEEN ('♛', Color.WHITE, 900,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();
        moves.addAll(BISHOP.movesFrom(board,position).toList());
        moves.addAll(ROOK.movesFrom(board,position).toList());
        return moves.stream();
    },
    new int[][] // boardWorth
    {
        {-20,-10,-10, -5, -5,-10,-10,-20}, // 1
        {-10,  0,  0,  0,  0,  0,  0,-10}, // 2
        {-10,  0,  5,  5,  5,  5,  0,-10}, // 3
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 4
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 5
        {-10,  5,  5,  5,  5,  5,  5,-10}, // 6
        {-10,  0,  5,  0,  0,  5,  0,-10}, // 7
        {-20,-10,-10, -5, -5,-10,-10,-20}  // 8
    }),

    KING  ('♚', Color.WHITE, 20000,
    (board,position) ->
    {
        List<int[]> moves = new ArrayList<>();

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                moves.add(new int[]{position[0] + i, position[1] + j,0});
            }
        }
        moves.remove(4);

        int castle = board.whiteAt(position) ? Board.WHITE_KING:Board.BLACK_KING;
        if (board.flag(castle++)=='c')
        {
            if (board.flag(castle++)=='c'
                    && board.at(position[0]-1,position[1])=='ㅤ'
                    && board.at(position[0]-2,position[1])=='ㅤ'
                    && board.at(position[0]-3,position[1])=='ㅤ')
                moves.add(new int[]{position[0]-2,position[1],-1});
            if (board.flag(castle  )=='c'
                    && board.at(position[0]+1,position[1])=='ㅤ'
                    && board.at(position[0]+2,position[1])=='ㅤ')
                moves.add(new int[]{position[0]+2,position[1], 1});
        }

        return moves.stream();
    },
    new int[][] // boardWorth
    {
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 1
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 2
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 3
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 4
        {-20,-30,-30,-40,-40,-30,-30,-20}, // 5
        {-10,-20,-20,-20,-20,-20,-20,-10}, // 6
        { 20, 20,  0,  0,  0,  0, 20, 20}, // 7
        { 20, 30, 10,  0,  0, 10, 20, 30}  // 8
    }),

    BLACK_PAWN  ('♙', Color.BLACK, -100, PAWN.pattern,
    new int[][] // boardWorth
    {
        {  0,  0,  0,  0,  0,  0,  0,  0}, // 8
        {  5, 10, 10,-20,-20, 10, 10,  5}, // 7
        {  5, -5,-10,  0,  0,-10, -5,  5}, // 6
        {  0,  0,  0, 20, 20,  0,  0,  0}, // 5
        {  5,  5, 10, 25, 25, 10,  5,  5}, // 4
        { 10, 10, 20, 30, 30, 20, 10, 10}, // 3
        { 50, 50, 50, 50, 50, 50, 50, 50}, // 2
        { 30, 30, 30, 30, 30, 30, 30, 30}, // 1
    }),

    BLACK_KNIGHT('♘', Color.BLACK, -320, KNIGHT.pattern,
    new int[][] // boardWorth
    {
        {-50,-40,-30,-30,-30,-30,-40,-50}, // 8
        {-40,-20,  0,  5,  5,  0,-20,-40}, // 7
        {-30,  5, 10, 15, 15, 10,  5,-30}, // 6
        {-30,  0, 15, 20, 20, 15,  0,-30}, // 5
        {-30,  0, 15, 20, 20, 15,  0,-30}, // 4
        {-30,  5, 10, 15, 15, 10,  5,-30}, // 3
        {-40,-20,  0,  0,  0,  0,-20,-40}, // 2
        {-50,-40,-30,-30,-30,-30,-40,-50}, // 1
    }),

    BLACK_BISHOP('♗', Color.BLACK, -330, BISHOP.pattern,
    new int[][] // boardWorth
    {
        {-20,-10,-10,-10,-10,-10,-10,-20}, // 8
        {-10,  5,  0,  0,  0,  0,  5,-10}, // 7
        {-10, 10, 10, 10, 10, 10, 10,-10}, // 6
        {-10,  0, 10, 10, 10, 10,  0,-10}, // 5
        {-10,  5,  5, 10, 10,  5,  5,-10}, // 4
        {-10,  0,  5, 10, 10,  5,  0,-10}, // 3
        {-10,  0,  0,  0,  0,  0,  0,-10}, // 2
        {-20,-10,-10,-10,-10,-10,-10,-20}, // 1
    }),

    BLACK_ROOK  ('♖', Color.BLACK, -500, ROOK.pattern,
    new int[][] // boardWorth
    {
        {  0,  0,  0,  5,  5,  0,  0,  0}, // 8
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 7
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 6
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 5
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 4
        { -5,  0,  0,  0,  0,  0,  0, -5}, // 3
        {  5, 10, 10, 10, 10, 10, 10,  5}, // 2
        {  0,  0,  0,  0,  0,  0,  0,  0}, // 1
    }),

    BLACK_QUEEN ('♕', Color.BLACK, -900, QUEEN.pattern,
    new int[][] // boardWorth
    {
        {-20,-10,-10, -5, -5,-10,-10,-20}, // 8
        {-10,  0,  5,  0,  0,  5,  0,-10}, // 7
        {-10,  5,  5,  5,  5,  5,  5,-10}, // 6
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 5
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 4
        {-10,  0,  5,  5,  5,  5,  0,-10}, // 3
        {-10,  0,  0,  0,  0,  0,  0,-10}, // 2
        {-20,-10,-10, -5, -5,-10,-10,-20}, // 1
    }),

    BLACK_KING  ('♔', Color.BLACK, -20000, KING.pattern,
    new int[][] // boardWorth
    {
        { 20, 30, 10,  0,  0, 10, 20, 30}, // 8
        { 20, 20,  0,  0,  0,  0, 20, 20}, // 7
        {-10,-20,-20,-20,-20,-20,-20,-10}, // 6
        {-20,-30,-30,-40,-40,-30,-30,-20}, // 5
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 4
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 3
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 2
        {-30,-40,-40,-50,-50,-40,-40,-30}, // 1
    }),

    VACANT('ㅤ', new Color(0,0,0,0),0,(board,position) -> Stream.empty(),new int[][]{});

    public static final String white = "♚♛♜♝♞♟";
    public static final String black = "♔♕♖♗♘♙";
    
    private static final int[] mirror = new int[]{-1,1};
    public  static int[] mirror(){return mirror;}
    private static final int[] mirror2 = new int[]{-2,2};
    public  static int[] mirror2(){return mirror2;}

    public static boolean isPiece(char c) {return c >= '♔' && c <= '♟';}
    public static boolean isWhite(char c) {return c >= '♚' && c <= '♟';}
    public static boolean isBlack(char c) {return c >= '♔' && c <= '♙';}

    public static Type fromChar(char c)
    {
        return switch (c)
        {
            case '♟' ->       PAWN  ;
            case '♞' ->       KNIGHT;
            case '♝' ->       BISHOP;
            case '♜' ->       ROOK  ;
            case '♛' ->       QUEEN ;
            case '♚' ->       KING  ;
            case '♙' -> BLACK_PAWN  ;
            case '♘' -> BLACK_KNIGHT;
            case '♗' -> BLACK_BISHOP;
            case '♖' -> BLACK_ROOK  ;
            case '♕' -> BLACK_QUEEN ;
            case '♔' -> BLACK_KING  ;
            default -> VACANT;
        };
    }

    public static Color color(char c)
    {
        return c < '♔' || c > '♟' ? VACANT.color : c < '♚' ? Color.BLACK : Color.WHITE;
//        return switch (c)
//        {
//            case '♚','♛','♜','♝','♞','♟' -> Color.WHITE;
//            case '♔','♕','♖','♗','♘','♙' -> Color.BLACK;
//            default -> VACANT.color;
//        };
    }

    public static int value(char c)
    {
        return switch (c)
        {
            case '♟' ->       PAWN  .value;
            case '♞' ->       KNIGHT.value;
            case '♝' ->       BISHOP.value;
            case '♜' ->       ROOK  .value;
            case '♛' ->       QUEEN .value;
            case '♚' ->       KING  .value;
            case '♙' -> BLACK_PAWN  .value;
            case '♘' -> BLACK_KNIGHT.value;
            case '♗' -> BLACK_BISHOP.value;
            case '♖' -> BLACK_ROOK  .value;
            case '♕' -> BLACK_QUEEN .value;
            case '♔' -> BLACK_KING  .value;
            default  -> 0;
        };
    }

    public static char invert(char piece)
    {
        if      (Type.isWhite(piece)) return (char)(piece-6);
        else if (Type.isBlack(piece)) return (char)(piece+6);
        else                          return  'ㅤ';
    }

    public static Type invert(Type piece)
    {
        if      (piece.isWhite()) return values()[piece.ordinal()+6];
        else if (piece.isBlack()) return values()[piece.ordinal()-6];
        else                      return VACANT;
    }

    public static Piece invert(Piece piece){return new Piece(invert(piece.icon()),piece.board,piece.position);}

    public  final char    icon;
    public  final String  sIcon;
    public  final Color   color;
    public  final int     value;
    private final int[][] posWorth;
    private final BiFunction<Board, int[],Stream<int[]>> pattern;

    Type(char icon, Color color, int value, BiFunction<Board, int[], Stream<int[]>> pattern, int[][] posWorth)
    {
        this.icon     = icon;
        this.sIcon    = ""+icon;
        this.color    = color;
        this.value    = value;
        this.pattern  = pattern;
        this.posWorth = posWorth;
    }

    public boolean isWhite (){return color == Color.WHITE;}
    public boolean isBlack (){return color == Color.BLACK;}
    public boolean isPiece (){return this  != VACANT;}
    public boolean isVacant(){return this  == VACANT;}
    public int     valueAt (int... position){try{return posWorth[position[0]][position[1]];}catch(IndexOutOfBoundsException ignored){return 0;}}
    public Stream<int[]> movesFrom(Board board, int[] position) // note: includes both moves onto white *and* black pieces regardless of Type
    {
        return pattern.apply(board,position).filter(p ->
                                                      p[0] <  8 && p[1] <  8
                                                   && p[0] > -1 && p[1] > -1); // filter out moves outside of board
    }

    @Override public String toString() {return sIcon;}
    public String Name(){return color.toString() + ' ' + name().replaceFirst("BLACK_","");}
}
