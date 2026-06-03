package ai.game.demo.chess;

import ai.game.demo.util.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static ai.game.demo.util.Direction.*;

public enum Type
{
    PAWN  ('♟', Color.WHITE, 100,
    (board,position) ->
    {
        List<Integer> moves = new ArrayList<>();

        //promotion pieces
        char[] PromotionPieces = promotionWhites();

        int move = position+SOUTH.x88;
        if (!board.pieceAt(move))
        {
            //promotion move
            if(position < 0x20)
            {
                for (int i = 1; i<5;i++)
                {
                    moves.add(move+(i<<8));
                }
            }
            else //normal move
            {
                moves.add(move);
                //double move
                if (position>=0x60)
                {
                    move += SOUTH.x88;
                    if (!board.pieceAt(move)) moves.add(move);
                }
            }
        }
        // diagonal moves, en passant included
        for (int dir : w_mirror())
        {
            move = position+dir;
            if (board.pieceAt(move))
            {
                //promotion move
                if(position < 0x20)
                {
                    for (int i = 1; i<5;i++)
                    {
                        moves.add(move+(i<<8));
                    }
                }
                else //normal move
                {
                    moves.add(move);
                }
            }
            else if (board.passantAt(move))
            {
                moves.add(move);
            }
        }

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
    }),

    KNIGHT('♞', Color.WHITE, 320,
    (board,position) ->
    {
        List<Integer> moves = new ArrayList<>();

        for (int i : knight())
        {
            moves.add(position+i);
            moves.add(position-i);
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
        List<Integer> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+ N_EAST.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+ S_EAST.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+ N_WEST.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+ S_WEST.x88*i);
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
        List<Integer> moves = new ArrayList<>();

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+NORTH.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+SOUTH.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+EAST.x88*i);
            if (board.pieceAt(moves.getLast())) break;
        }

        for (int i = 1; i < 8; i++)
        {
            moves.add(position+WEST.x88*i);
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
            Stream.concat(
                    BISHOP.movesFrom(board,position),
                    ROOK.movesFrom(board,position)),
    new int[][] // boardWorth
    {
        {-20,-10,-10, -5, -5,-10,-10,-20}, // 1
        {-10,  0,  0,  0,  0,  0,  0,-10}, // 2
        {-10,  0,  5,  5,  5,  5,  0,-10}, // 3
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 4
        { -0,  0,  5,  5,  5,  5,  0, -0}, // 5
        {-10,  5,  5,  5,  5,  5,  0,-10}, // 6
        {-10,  0,  5,  0,  0,  0,  0,-10}, // 7
        {-20,-10,-10, -5, -5,-10,-10,-20}  // 8
    }),

    KING  ('♚', Color.WHITE, 20000,
    (board,position) ->
    {
        List<Integer> moves = new ArrayList<>();

        for (Direction dir : Direction.values())
        {
            moves.add(position+dir.x88);
        }

        Color turn = board.turn();
        int castle = board.at(position).isWhite() ? Board.CASTLE_WHITE :Board.CASTLE_BLACK;
        if (board.flag(castle++)=='c')
        {
            if (board.flag(castle++)=='c'
                    && board.at(position-1).icon=='ㅤ' && !board.isCheck(turn,position-1)
                    && board.at(position-2).icon=='ㅤ' && !board.isCheck(turn,position-2)
                    && board.at(position-3).icon=='ㅤ' && !board.isCheck(turn,position-3))
                moves.add(position-2);
            if (board.flag(castle  )=='c'
                    && board.at(position+1).icon=='ㅤ' && !board.isCheck(turn,position+1)
                    && board.at(position+2).icon=='ㅤ' && !board.isCheck(turn,position+2))
                moves.add(position+2);
        }

        return moves.stream().filter(pos->!board.isCheck(turn,pos));
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

    BLACK_PAWN  ('♙', Color.BLACK, -100,
    (board,position) ->
    {
        List<Integer> moves = new ArrayList<>();

        //promotion pieces
        char[] PromotionPieces = promotionBlacks();

        int move = position+SOUTH.x88;
        if (!board.pieceAt(move))
        {
            //promotion move
            if(position>=0x60)
            {
                for (int i = 1; i<5;i++)
                {
                    moves.add(move+(i<<8));
                }
            }
            else //normal move
            {
                moves.add(move);
                //double move
                if (position < 0x20)
                {
                    move += SOUTH.x88;
                    if (!board.pieceAt(move)) moves.add(move);
                }
            }
        }
        // diagonal moves, en passant included
        for (int dir : b_mirror())
        {
            move = position+dir;
            if (board.pieceAt(move))
            {
                //promotion move
                if(position>=0x60)
                {
                    for (int i = 1; i<5;i++)
                    {
                        moves.add(move+(i<<8));
                    }
                }
                else //normal move
                {
                    moves.add(move);
                }
            }
            else if (board.passantAt(move))
            {
                moves.add(move);
            }
        }

        return moves.stream();
    }, PAWN.valuePos),

    BLACK_KNIGHT('♘', Color.BLACK, -320, KNIGHT.pattern, KNIGHT.valuePos),

    BLACK_BISHOP('♗', Color.BLACK, -330, BISHOP.pattern, BISHOP.valuePos),

    BLACK_ROOK  ('♖', Color.BLACK, -500, ROOK.pattern, ROOK.valuePos),

    BLACK_QUEEN ('♕', Color.BLACK, -900, QUEEN.pattern, QUEEN.valuePos),

    BLACK_KING  ('♔', Color.BLACK, -20000, KING.pattern, KING.valuePos),

    VACANT('ㅤ', new Color(0,0,0,0),0,(board,position) -> Stream.empty(),new int[][]{});

    public static final String white = "♚♛♜♝♞♟";
    public static final String black = "♔♕♖♗♘♙";
    
    private static final int[] w_mirror = new int[]{S_WEST.x88, S_EAST.x88};
    public  static int[] w_mirror(){return w_mirror;}
    private static final int[] b_mirror = new int[]{N_WEST.x88, N_EAST.x88};
    public  static int[] b_mirror(){return b_mirror;}
    private static final char[] promotionWhites = new char[]{'♛','♝','♞','♜'};
    private static final char[] promotionBlacks = new char[]{'♕','♗','♘','♖'};
    public static char[] promotionWhites(){return promotionWhites;}
    public static char[] promotionBlacks(){return promotionBlacks;}

    static final int[] knight = new int[]{0x21, 0x1F, 0x12, 0x0E};
    static int[] knight(){return knight;}

    public static boolean isPiece(char c) {return c >= '♔' && c <= '♟';}
    public static boolean isWhite(char c) {return c >= '♚' && c <= '♟';}
    public static boolean isBlack(char c) {return c >= '♔' && c <= '♙';}

    public static Type from(int c){return from(((char)c));}
    public static Type from(char c)
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
        return switch (piece)
        {
            case '♟' -> '♙';
            case '♞' -> '♘';
            case '♝' -> '♗';
            case '♜' -> '♖';
            case '♛' -> '♕';
            case '♚' -> '♔';
            case '♙' -> '♟';
            case '♘' -> '♞';
            case '♗' -> '♝';
            case '♖' -> '♜';
            case '♕' -> '♛';
            case '♔' -> '♚';
            default  -> 'ㅤ';
        };
//        if      (Type.isWhite(piece)) return (char)(piece-6);
//        else if (Type.isBlack(piece)) return (char)(piece+6);
//        else                          return VACANT.icon;
    }

    public static Type invert(Type piece)
    {
        return switch (piece)
        {
            case       PAWN   -> BLACK_PAWN  ;
            case       KNIGHT -> BLACK_KNIGHT;
            case       BISHOP -> BLACK_BISHOP;
            case       ROOK   -> BLACK_ROOK  ;
            case       QUEEN  -> BLACK_QUEEN ;
            case       KING   -> BLACK_KING  ;
            case BLACK_PAWN   ->       PAWN  ;
            case BLACK_KNIGHT ->       KNIGHT;
            case BLACK_BISHOP ->       BISHOP;
            case BLACK_ROOK   ->       ROOK  ;
            case BLACK_QUEEN  ->       QUEEN ;
            case BLACK_KING   ->       KING  ;
            default -> VACANT;
        };
//        if      (piece.isWhite()) return values()[piece.ordinal()+6];
//        else if (piece.isBlack()) return values()[piece.ordinal()-6];
//        else                      return VACANT;
    }

    public static Piece invert(Piece piece){return new Piece(invert(piece.type),piece.board,piece.position);}

    public  final char    icon;
    public  final String  sIcon;
    public  final Color   color;
    public  final int     value;
    private final int[][] valueAt;
    private final int[][] valuePos;
    private final BiFunction<Board, Integer,Stream<Integer>> pattern;

    Type(char icon, Color color, int value, BiFunction<Board, Integer, Stream<Integer>> pattern, int[][] valuePos)
    {
        this.icon     = icon;
        this.sIcon    = ""+icon;
        this.color    = color;
        this.value    = value;
        this.pattern  = pattern;
        this.valuePos = color==Color.WHITE
                        ? valuePos
                        // "simple" stream for rotating 2dim array (and inverting the values therein)
                        : Arrays.stream(valuePos).map(i -> Arrays.stream(i).map(v -> -v).toArray())
//                                .boxed()                                                             // box ints for reversible List  |  keen minds know chessboards
//                                .toList().reversed().stream().mapToInt(Integer::intValue).toArray()) // reverse columns (and unbox)   |  are *mirrored* between sides
                                .toList().reversed().toArray(int[][]::new);                            // reverse rows
        this.valueAt  = Arrays.stream(this.valuePos).map(i->Arrays.stream(i).map(v->v+value).toArray()).toArray(int[][]::new);
    }

    public Type invert() {return invert(this);}

    public Type    type    (){return icon<KING.icon?invert(this):this;}
    public boolean isType  (char type){return isType(from(type));}
    public boolean isType  (Type type){return type==type();}

    public boolean isTurn  (char turn){return turn==(color==Color.WHITE?'w':'b');}

    public boolean isWhite (){return color == Color.WHITE;}
    public boolean isBlack (){return color == Color.BLACK;}
    public boolean isPiece (){return this  != VACANT;}
    public boolean isVacant(){return this  == VACANT;}

    public int     valueAt (int    position){return valueAt(Board.map[position]);}
    public int     valueAt (int... position){try{return valueAt [position[0]][position[1]];}catch(IndexOutOfBoundsException ignored){return 0;}}
    public int     valueOf (int    position){return valueOf(Board.map[position]);}
    public int     valueOf (int... position){try{return valuePos[position[0]][position[1]];}catch(IndexOutOfBoundsException ignored){return 0;}}
    public Stream<Integer> movesFrom(Board board, int position) // note: includes both moves onto white *and* black pieces
    // regardless of Type
    {
        if(board.checks==null||this.type()==KING) return movesUnchecked(board,position); // the KINGs pattern handles checks itself
        return board.checks.length<1
             ? Stream.empty() // if more than one piece threatens the king, the king *itself* must be moved to avoid capture
             : movesUnchecked(board,position).filter(move-> Arrays.stream(board.checks).anyMatch(pos->pos==move)); // if KING in check, filter moves to those that intercept
    }
    public Stream<Integer> movesUnchecked(Board board, int position)
    {
        return pattern.apply(board,position).filter(p -> (p & 0x88)==0||type()==PAWN) // filter out moves outside of board
                                            .filter(m -> board.at(m).color != color); // filter out allied pieces
    }

    @Override public String toString() {return sIcon;}
    public String Name(){return color.toString() + ' ' + type().name();}
}
