package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ai.game.demo.chess.Type.*;

public class Board extends State<Board> implements Comparable<Board>
{
    private static int flags=0;
    private static final String[] initialFlags= new String[]{"a1a1wxycccccc"};
    public  static final int TO_X, TO_Y, FROM_X, FROM_Y, TURN, PASSANT_X, PASSANT_Y,
                             BLACK_KING, BLACK_LEFT_ROOK, BLACK_RIGHT_ROOK,
                             WHITE_KING, WHITE_LEFT_ROOK, WHITE_RIGHT_ROOK;
    
    static // set flag indexes
    {
        TO_X=flags++;
        TO_Y=flags++;
        FROM_X=flags++;
        FROM_Y=flags++;
        TURN=flags++;
        PASSANT_X=flags++;
        PASSANT_Y=flags++;
        BLACK_KING=flags++;
        BLACK_LEFT_ROOK=flags++;
        BLACK_RIGHT_ROOK=flags++;
        WHITE_KING=flags++;
        WHITE_LEFT_ROOK=flags++;
        WHITE_RIGHT_ROOK=flags++;
    }

    public record Dto(char[][] board){};
    public Dto toDto() {return new Dto(board);}

    private final int hashcode;
    private final char[][] board;

    private Board(char[][] board) {this.board = board;this.hashcode = toString().hashCode();}
    public Board(String[] board)
    {
        this.board = new char[9][];
        for (int i = 0; i < board.length; i++) this.board[i] = board[i].toCharArray();
        if (board.length==8) this.board[8]=initialFlags[0].toCharArray();
        else if (board.length<9) throw new IllegalArgumentException("ChessBoard Bad Length");
        if (Stream.of(this.board).limit(8).anyMatch(row->row.length!=8))
            throw new IllegalArgumentException("ChessBoard Bad Width");
        if (this.board[8].length!=flags)
            throw new IllegalArgumentException("ChessBoard Bad MetaData");
        this.hashcode = toString().hashCode();
    }
    public Board(String board){this(Stream.of(
            board.substring(0,64).split("(?<=\\G........)"),
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

    public char[][] raw() {return Arrays.stream(this.board).limit(8).map(char[]::clone).toArray(char[][]::new);}
    public char     flag(int index){return board[8][index];}
//    public void     setFlag(int index){board[8][index]++;}

    public Piece    getPiece   (String pos) {return getPiece(normalize(pos.toCharArray()));}
    public Piece    getPiece   (int... pos) {return new Piece(at(pos), this, pos);}
    public boolean  whiteAt    (int... pos) {return Type.isWhite(at(pos));}
    public boolean  blackAt    (int... pos) {return Type.isBlack(at(pos));}
    public boolean  pieceAt    (int... pos) {return Type.isPiece(at(pos));}
    public char     at         (int... pos) {try{return board[pos[1]][pos[0]];}catch (ArrayIndexOutOfBoundsException e) {return ' ';}}

//    protected char set(Piece piece, int...pos) {return set(piece.icon(),pos);}
//    protected char set(char  piece, int...pos)
//    {
//        char past = board[pos[1]][pos[0]];
//        board[pos[1]][pos[0]] = piece;
//        return past;
//    }

    public boolean maximize(){return board[8][4]=='w';}
    public Board doWhite(int depth){return this.minMax(depth).furthestAncestor();}
    public Board doBlack(int depth){return this.minMax(depth).furthestAncestor();}
    public Board doWhite(){return this.minMax().furthestAncestor();}
    public Board doBlack(){return this.minMax().furthestAncestor();}

    public  List<Actionable<Board>> whites(){return pieces(Type::isWhite);}
    public  List<Actionable<Board>> blacks(){return pieces(Type::isBlack);}
    public  List<Actionable<Board>> pieces(){return pieces(Type::isPiece);}
    private List<Actionable<Board>> pieces(Predicate<Character> condition)
    {
        List<Actionable<Board>> pieces = new ArrayList<>();
        char file, rank = 0;
        for (char[] s : board)
        {
            file = 0;
            for (char c : s)
            {
                if (condition.test(c)) pieces.add(new Piece(c, this, file, rank));
                file++;
            }
            rank++;
        }
        return pieces;
    }

    public int score()
    {
        int r=0,c=0,buffer = 0;
        for (char[] row : board)
        {
            for (char piece : row)
            {
                buffer += Type.value(piece) + Type.fromChar(piece).valueAt(r,c);
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
            for (int[] p : type.movesFrom(this,position).filter(p -> Type.fromChar(at(p)) == type).toList())
            {
                sum += Type.value(at(p)); // not just 'type's value, as type at p may be *black* piece
            }
        }

        for (int i : Type.mirror()) // own logic for pawns as they move differently when capturing
        {
            if (at(position[0]+1,position[1]+i) ==       PAWN.icon) sum +=       PAWN.value;
            if (at(position[0]-1,position[1]+i) == BLACK_PAWN.icon) sum += BLACK_PAWN.value;
        }
        return sum;
    }

    public Stream<int[]> movesFor(int... position){return Type.fromChar(at(position[0], position[1])).movesFrom(this, position);}
    public int[] isLegalMove(String move) {return move.split(",").length == 2
                                                ? isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim())
                                                : null;}
    public int[] isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),
                                                                        normalize(to.toCharArray()));}
    public int[] isLegalMove(int[] from, int[] to)
    {
        if(Type.color(at(from))==Type.color(at(to))) return null;
        Type piece = Type.fromChar(at(from));
        return movesFor(from).filter(m -> Type.color(at(m)) != piece.color)
                             .filter(m -> m[0]==to[0]&&m[1]==to[1])
                             .findAny().orElse(null);
    }

    public Board move(String move) {return move(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public Board move(String from, String to) {return move(normalize(from.toCharArray()),isLegalMove(from+','+to));}
    public Board move(int[] from, int[] to)
    {
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        int fromX = from[0];
        int fromY = from[1];
        int   toX =   to[0];
        int   toY =   to[1];

        board[to[1]][to[0]] = board[from[1]][from[0]]; // put moved piece to target location
        board[from[1]][from[0]] = VACANT.icon;        //  erase moved piece from previous location
        board[8][  TO_X] = (char)to  [0]; board[8][  TO_Y] = (char)to  [1];  // update metadata 'moved to'
        board[8][FROM_X] = (char)from[0]; board[8][FROM_Y] = (char)from[1];  // update metadata 'moved from'
        board[8][TURN] = board[8][TURN] == 'w' ? 'b' : 'w';  // update identity of active turn

        castling(board,to); // apply castling rules

        int yDistance = toY-fromY;
        //basic en passant logic :/ check pawn
        int[] passantTarget = new int[2];
        boolean pawn = isPawn(board[toY][toX]);
        if (pawn){
            //take en passant target
            if (board[8][5] == toX && board[8][6] == toY)
            {
                board[fromY][toX] = VACANT.icon;
            }else {
                //en passant availability check
                boolean enPassantAvailable = Math.abs(yDistance) == 2;
                passantTarget[0] = enPassantAvailable ?  toX : 0;
                passantTarget[1] = enPassantAvailable ? (toY - (yDistance / 2)) : 0;
            }
        }
        board[8][5] = (char) passantTarget[0];
        board[8][6] = (char) passantTarget[1];
        // todo: update metadata

        return new Board(board);
    }

    private boolean isPawn(char piece){return (piece=='♙'||piece=='♟');}

    public boolean passantAt(int[] passantPos){return(board[8][5]==passantPos[0]&&board[8][6]==passantPos[1]);}

    private void castling(char[][] board,int[] move)
    {
        if(move.length>2) // castling
        {
            if(move[2]<0){board[move[1]][move[0]+1]= board[move[1]][0];board[move[1]][0]= VACANT.icon;} // left
            else         {board[move[1]][move[0]-1]= board[move[1]][7];board[move[1]][7]= VACANT.icon;}// right
            board[8][move[1]>0?7:10]=' '; // erase king castling-flag
        }
        else if (board[0][0]!='♖')board[8][BLACK_LEFT_ROOK ]=' '; // check if expected piece is present.
        else if (board[0][7]!='♖')board[8][BLACK_RIGHT_ROOK]=' '; // the alternative would be to check
        else if (board[7][0]!='♜')board[8][WHITE_LEFT_ROOK ]=' '; // if *either* to or from each if they
        else if (board[7][7]!='♜')board[8][WHITE_RIGHT_ROOK]=' '; // match coordinates, ie double the checks
    }


    @Override protected int hashIdentifier (){return hashcode;}
    @Override protected int evaluateFitness(){return score ();}
    @Override
    public List<Actionable<Board>> getActionables(boolean isBlackTurn)
    {
        return isBlackTurn ? blacks() : whites();
    }

    public String toString() // simplified String to use for hashCode
    {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(""+board[8][4]);
        Arrays.stream(board).limit(8).forEach(row -> joiner.add(String.valueOf(row)));
//        for (char[] s : board) joiner.add(String.valueOf(s));
        return joiner.toString();
    }

    /// below methods primarily used to format data for/from readability ///

    public static void announceCapture(Piece taker, Piece taken)
    {
        System.out.println("\033[33;3m" + taker.color() + ' ' + taker.name()
                                   + " \tcaptures " + taken.color() + ' '
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

                if (Type.isPiece(board[i][j])) joiner.add(space + board[i][j] + space);
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

                if (Type.isPiece(board[i][j])) joiner.add(" " + board[i][j]);
                else joiner.add(square);
            }
//            joiner.add(" "+i);
            joiner.add("\n");
        }

        joiner.add("    a   b   c   d   e   f   g   h");
        joiner.add("\n").add(board[8][4]+" "+letterize(new int[]{board[8][2], board[8][3]},new int[]{board[8][0], board[8][1]}));

        return joiner.toString();
    }
}
