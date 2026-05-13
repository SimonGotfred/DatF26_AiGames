package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ai.game.demo.chess.Type.*;

public class Board extends State<Board> implements Comparable<Board>
{
    private static int flags=0;
    private static final String[] initialFlags= new String[]{"a1a1wpxycccccc"}; // ! yes, there is a reason for this being an array
    public  static final int TO_X, TO_Y, FROM_X, FROM_Y, TURN, PROMOTION, PASSANT_X, PASSANT_Y,
                             BLACK_KING, BLACK_LEFT_ROOK, BLACK_RIGHT_ROOK,
                             WHITE_KING, WHITE_LEFT_ROOK, WHITE_RIGHT_ROOK;

    static // set flag indexes
    {
        TO_X=flags++;
        TO_Y=flags++;
        FROM_X=flags++;
        FROM_Y=flags++;
        TURN=flags++;
        PROMOTION=flags++;
        PASSANT_X=flags++;
        PASSANT_Y=flags++;
        BLACK_KING=flags++;
        BLACK_LEFT_ROOK=flags++;
        BLACK_RIGHT_ROOK=flags++;
        WHITE_KING=flags++;
        WHITE_LEFT_ROOK=flags++;
        WHITE_RIGHT_ROOK=flags++;
    }

    public record Dto(Type[][] board){};
    public Dto toDto() {return new Dto(board);}

    private final int hashcode;
    private final Type[][] board;
    private final char[] metadata;// = initialFlags[0].toCharArray();

    public Board(char[][] board)
    {
        int r=0,c=0;
        this.board = new Type[board.length][];
        for (char[] row : board)
        {
            this.board[r] = new Type[row.length];
            for (char col : row)
            {
                this.board[r][c] = Type.from(col);
                c++;
            }
            r++; c=0;
        }
        this.metadata=board.length>8?board[8]:initialFlags[0].toCharArray();
        this.hashcode=toString().hashCode();
    }
    public Board(Type[][] board) {this(board,initialFlags[0]);}
    public Board(Type[][] board,String meta) {this.board=board;this.metadata=meta.toCharArray();this.hashcode=toString().hashCode();}
    public Board(String[] board)
    {
        this.board = new Type[8][];
        for (int i = 0; i < 8; i++) this.board[i] = board[i].chars().mapToObj(Type::from).toArray(Type[]::new);
        if (board.length==8) this.metadata=initialFlags[0].toCharArray();
        else if (board.length<9) throw new IllegalArgumentException("ChessBoard Bad Length");
        else metadata = board[8].toCharArray();
        if (Stream.of(this.board).limit(8).anyMatch(row->row.length!=8))
            throw new IllegalArgumentException("ChessBoard Bad Width");
        if (board[8].length()!=flags)
            throw new IllegalArgumentException("ChessBoard Bad MetaData");
        this.hashcode = toString().hashCode();
    }
    public Board(String board)
    {this(Stream.of(board.substring(0,64).split("(?<=\\G........)"),
                    board.length()>64 ? new String[]{board.substring(64)} : initialFlags)
                                                          .flatMap(Stream::of).toArray(String[]::new));}
    public Board()
    {
        this(new String[] // ! ALL HAIL THE GLORIOUS 'ㅤ' IT STRUCK UPON US FROM BETWEEN THE HANGUL !
        {
            "♖♘♗♕♔♗♘♖",
            "♙♙♙♙♙♙♙♙",
            "ㅤㅤㅤㅤㅤㅤㅤㅤ",
            "ㅤㅤㅤㅤㅤㅤㅤㅤ",
            "ㅤㅤㅤㅤㅤㅤㅤㅤ",
            "ㅤㅤㅤㅤㅤㅤㅤㅤ",
            "♟♟♟♟♟♟♟♟",
            "♜♞♝♛♚♝♞♜",
            initialFlags[0]
        });
        //8,0-3 prev pos
        //8,4 current turn
        //8,5-6 en passant target
        //8,7 white king castling legality
        //8,8 white left tower castling legality
        //8,9 white right tower castling legality
        //8,10 black white king move castling legality
        //8,11 black left tower castling legality
        //8,12 black right tower castling legality
    }

    public Type[][] raw() {return Arrays.stream(this.board).limit(8).map(Type[]::clone).toArray(Type[][]::new);}
    public char flag(int index){return metadata[index];}
    
    public Piece    getPiece   (int...  pos) {return new Piece(at(pos).icon, this, pos);}
    public boolean  whiteAt    (int...  pos) {return at(pos).isWhite(   );}
    public boolean  blackAt    (int...  pos) {return at(pos).isBlack(   );}
    public boolean  pieceAt    (int...  pos) {return at(pos).isPiece(   );}
    public int      valueAt    (int...  pos) {return at(pos).valueOf(pos);}
    public Type     at         (int...  pos) {try{return board[pos[1]][pos[0]];}catch (ArrayIndexOutOfBoundsException e) {return VACANT;}}
    public Type     at         (String  pos) {return at(normalize(pos.toCharArray()));}

    public boolean maximize(){return metadata[4]=='w';}
    public Board   doWhite(int depth){return this.minMax(depth).furthestAncestor();}
    public Board   doBlack(int depth){return this.minMax(depth).furthestAncestor();}
    public Board   doWhite(){return this.minMax().furthestAncestor();}
    public Board   doBlack(){return this.minMax().furthestAncestor();}

    public  List<Actionable<Board>> whites(){return pieces(Type::isWhite);}
    public  List<Actionable<Board>> blacks(){return pieces(Type::isBlack);}
    public  List<Actionable<Board>> pieces(){return pieces(Type::isPiece);}
    private List<Actionable<Board>> pieces(Predicate<Character> condition)
    {
        List<Actionable<Board>> pieces = new ArrayList<>();
        char file, rank = 0;
        for (Type[] s : board)
        {
            file = 0;
            for (Type c : s)
            {
                if (condition.test(c.icon)) pieces.add(new Piece(c.icon, this, file, rank));
                file++;
            }
            rank++;
        }
        return pieces;
    }

    public int score()
    {
        int r=0,c=0,buffer = 0;
        for (Type[] row : board)
        {
            for (Type piece : row)
            {
                buffer += piece.valueAt(r, c);
                c++;
            }
            r++; c=0;
        }
        return buffer;
    }

    public static char[][] invert(char[][] board) // ! not functional
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

    private static final Type[] simple =  new Type[]{KNIGHT, BISHOP, ROOK, QUEEN, KING};
    public int riskAt(int...   position) // sum of pieces threatening the location, by using their patterns reversed
    {
//        if (!(position[0] < 8 && position[1] < 8)) return 0; // skip non-pathable positions

        int sum = 0;
        for (Type type : simple) // pattern for black/white pieces are mostly identical, so only
        {                        //  run each pattern once, collecting both corresponding black/white
            for (int[] p : type.movesFrom(this,position).filter(p->at(p)==type||at(p)==type.invert()).toList())
            {
                sum += at(p).value; // not just 'type's value, as type at p may be *black* piece
            }
        }

        for (int i : Type.mirror()) // own logic for pawns as they move differently when capturing
        {
            if (at(position[0]+1,position[1]+i) ==       PAWN) sum +=       PAWN.value;
            if (at(position[0]-1,position[1]+i) == BLACK_PAWN) sum += BLACK_PAWN.value;
        }
        return sum;
    }

    public Stream<int[]> movesFor(int... position){return at(position[0], position[1]).movesFrom(this, position);}
    public int[] isLegalMove(String move) {return move.split(",").length == 2
                                                ? isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim())
                                                : null;}
    public int[] isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),
                                                                        normalize(to.toCharArray()));}
    public int[] isLegalMove(int[] from, int[] to)
    {
        if(at(from).color==at(to).color) return null;
        Type piece = at(from);
        return movesFor(from).filter(m -> at(m).color != piece.color)
                             .filter(m -> m[0]==to[0]&&m[1]==to[1])
                             .findAny().orElse(null);
    }

    public Board move(String move) {return move(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public Board move(String from, String to) {return move(normalize(from.toCharArray()),isLegalMove(from+','+to));}
    public Board move(int[] from, int[] to)
    {
        Type[][] board = Arrays.stream(this.board).map(Type[]::clone).toArray(Type[][]::new);
        int fromX = from[0];
        int fromY = from[1];
        int   toX =   to[0];
        int   toY =   to[1];

        board[to[1]][to[0]] = board[from[1]][from[0]]; // put moved piece to target location
        board[from[1]][from[0]] = VACANT;        //  erase moved piece from previous location
        
        metadata[  TO_X] = (char)to  [0]; metadata[  TO_Y] = (char)to  [1];  // update metadata 'moved to'
        metadata[FROM_X] = (char)from[0]; metadata[FROM_Y] = (char)from[1];  // update metadata 'moved from'
        metadata[TURN] = metadata[TURN] == 'w' ? 'b' : 'w';  // update identity of active turn

        castling(board,to); // apply castling rules

        int yDistance = toY-fromY;
        //basic en passant logic :/ check pawn
        int[] passantTarget = new int[2];
        if (board[toY][toX].isType(PAWN))
        {
            //take en passant target
            if (metadata[5] == toX && metadata[6] == toY)
            {
                board[fromY][toX] = VACANT;
            }else {
                //en passant availability check
                boolean enPassantAvailable = Math.abs(yDistance) == 2;
                passantTarget[0] = enPassantAvailable ?  toX : 0;
                passantTarget[1] = enPassantAvailable ? (toY - (yDistance / 2)) : 0;
            }
        }
        metadata[5] = (char) passantTarget[0];
        metadata[6] = (char) passantTarget[1];
        // todo: update metadata

        return new Board(board);
    }

    private boolean isPawn(char piece){return (piece=='♙'||piece=='♟');}

    public boolean passantAt(int... passantPos){return(metadata[5]==passantPos[0]&&metadata[6]==passantPos[1]);}

    private void castling(Type[][] board,int[] move)
    {
        if(move.length>2) // castling
        {
            if      (move[2]<0){board[move[1]][move[0]+1]= board[move[1]][0];board[move[1]][0]= VACANT;} // left
            else if (move[2]>0){board[move[1]][move[0]-1]= board[move[1]][7];board[move[1]][7]= VACANT;}// right
            metadata[move[1]>1?WHITE_KING:BLACK_KING]=' '; // erase king castling-flag
        }
        if(metadata[BLACK_KING]!=' ')
        {
            if (board[0][0] != BLACK_ROOK) metadata[ BLACK_LEFT_ROOK] = ' '; // check if expected piece is present.
            if (board[0][7] != BLACK_ROOK) metadata[BLACK_RIGHT_ROOK] = ' '; // the alternative would be to check
        }
        if(metadata[WHITE_KING]!=' ')
        {
            if (board[7][0] != ROOK) metadata[ WHITE_LEFT_ROOK] = ' '; // if *either* to or from each if they
            if (board[7][7] != ROOK) metadata[WHITE_RIGHT_ROOK] = ' '; // match coordinates, ie double the checks
        }
    }

    @Override protected int hashIdentifier (){return hashcode;}
    @Override protected int evaluateFitness(){return score ();}
    @Override
    public List<Actionable<Board>> getActionables(boolean isBlackTurn)
    {
        return isBlackTurn ? blacks() : whites();
    }

    @Override
    public TreeSet<Action<Board>> getActions(boolean isBlackTurn)
    {
        Color color = isBlackTurn ? Color.BLACK : Color.WHITE;
        TreeSet<Action<Board>> actions = new TreeSet<>();
        for (int row=0; row<8; row++)
        {
            for (int col=0; col<8; col++)
            {
                if(board[row][col].color == color)
                {
                    int[] pos = new int[]{row, col};
                    for (int[] move : board[row][col].movesFrom(this, pos).toList())
                    {
                        actions.add(new State.Action<>(this)
                        {
                            @Override public Board apply(Board board){return board.move(pos,move);}
                            @Override public int evaluateFitness()   {return board[move[0]][move[1]].value+board[pos[0]][pos[1]].valueOf(move)+state.riskAt(move);}
                        });
                    }
                }
            }
        }

        return actions;
    }

    public String toString() // simplified String to use for hashCode
    {
        StringJoiner joiner = new StringJoiner("\n");
//        joiner.add(""+metadata[4]);
//        Arrays.stream(board).limit(8).forEach(row -> joiner.add(String.valueOf(row)));
        for (Type[] s : board) joiner.add(String.valueOf(s));
        joiner.add(String.valueOf(metadata));
        return joiner.toString();
    }

    /// below methods primarily used to format data for/from readability ///

    public static void announceCapture(Type taker, Type taken)
    {
        System.out.println("\033[33;3m" + taker.color + ' ' + taker.name()
                                   + " \tcaptures " + taken.color + ' '
                                   + taken.name() + "\033[0m");
    }

    public static int[]  normalize(char[] pos)
    {
        return new int[]
        {
               pos[0]-'a',
            7-(pos[1]-'1')
        };
    }
    public static char   numberize(char c){return (char)('8'-c);}
    public static char   letterize(char c){return        c+='A';} // neat way to implicitly cast result to char
    public static String letterize(int[] pos)
    {
        if(pos[0]<8) pos[0] += 'A';
        pos[1] = (char)('8'-pos[1]);
        if(pos[1]<8) pos[1] += '1';
        return ""+pos[0]+pos[1];
    }

    public String letterize(int[] from, int[] to){return ("-> "+at(to)+" "+letterize(from)+" to "+letterize(to));}

    public String toObsidian() // aligns nicely in Obsidian
    {
        StringJoiner joiner = new StringJoiner("");
        String square = "░";
        String space  = "     ";

        joiner.add("```\n");
//        joiner.add("       0  1  2  3  4  5  6  7\n");

        for (int i = 0; i < 8; i++)
        {
            joiner.add((8-i)+" ");
            for (int j = 0; j < board[i].length; j++)
            {
                if ((i+j) % 2 != 0) square = "░░";
                else                square = "    ";

                if (board[i][j].isPiece()) joiner.add(space + board[i][j] + space);
                else joiner.add(square);
            }
            joiner.add(" "+i+"\n");
        }

//        joiner.add("       a  b  c  d  e  f  g  h");
        joiner.add("\n```");

        return joiner.toString();
    }

    public String toConsole() // aligns nicely in console
    {
        StringJoiner joiner = new StringJoiner("");
        String square = "░";

//        joiner.add("    0   1   2   3   4   5   6   7\n");

        for (int i = 0; i < 8; i++)
        {
            joiner.add((8-i)+" ");
            for (int j = 0; j < board[i].length; j++)
            {
                if ((i+j) % 2 != 0) square = "░░";
                else                square = " ㅤ";

                if (board[i][j].isPiece()) joiner.add(" " + board[i][j]);
                else joiner.add(square);
            }
//            joiner.add(" "+i);
            joiner.add("\n");
        }

        joiner.add("    a   b   c   d   e   f   g   h");
        joiner.add("\n").add(metadata[4]+" "+letterize(new int[]{metadata[2], metadata[3]},new int[]{metadata[0], metadata[1]}));

        return joiner.toString();
    }
}
