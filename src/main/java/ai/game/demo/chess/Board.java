package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ai.game.demo.chess.Type.*;

public class Board extends State<Board> implements Comparable<Board>
{
    public record Dto(char[][] board){};
    public Dto toDto() {return new Dto(board);}

    private final int hashcode;
    private final char[][] board;

    public Board(char[][] board) {this.board = board;this.hashcode = toString().hashCode();}
    public Board(String[] board)
    {
        this.board = new char[board.length][];
        for (int i = 0; i < board.length; i++) this.board[i] = board[i].toCharArray();
        this.hashcode = toString().hashCode();
    }
    public Board(String board){this((board+"a1a1wpwwwbbb").split(","));}
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
            "a1a1wpwwwbbb"
        });
        //8,0-3 prev pos
        //8,4 current turn
        //8,5 en passant legality
        //8,6 white king castling legality
        //8,7 white left tower castling legality
        //8,8 white right tower castling legality
        //8,9 black white king move castling legality
        //8,10 black left tower castling legality
        //8,11 black right tower castling legality
    }

    public char[][] raw() {return Arrays.stream(this.board).limit(8).map(char[]::clone).toArray(char[][]::new);}
    public Piece    getPiece   (String  pos) {return getPiece(normalize(pos.toCharArray()));}
    public Piece    getPiece   (int... pos) {return new Piece(at(pos), this, pos);}
    public boolean  whiteAt    (int... pos) {return Type.isWhite(at(pos));}
    public boolean  blackAt    (int... pos) {return Type.isBlack(at(pos));}
    public boolean  pieceAt    (int... pos) {return Type.isPiece(at(pos));}
    public char     at         (int... pos) {try{return board[pos[1]][pos[0]];}catch (ArrayIndexOutOfBoundsException e) {return ' ';}}
    protected char  set        (char  piece, int...pos)
    {
        char past = board[pos[1]][pos[0]];
        board[pos[1]][pos[0]] = piece;
        return past;
    }
    protected char set(Piece piece, int...pos) {return set(piece.icon(),pos);}

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

    private static final Type[] simple =  new Type[]{KNIGHT, BISHOP, ROOK, QUEEN, KING};
    public int riskAt(int...   position) // sum of pieces threatening the location, by using their patterns reversed
    {
        if (!(position[0] < 8 && position[1] < 8)) return 0; // skip non-pathable positions

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
    public boolean    isLegalMove(String move) {return move.split(",").length == 2 && isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public boolean    isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public boolean    isLegalMove(int[] from, int[] to)
    {
        if(Type.color(at(from))==Type.color(at(to))) return false;
        Type piece = Type.fromChar(at(from));
        return movesFor(from).filter(m -> Type.color(at(m)) != piece.color).anyMatch(m -> Arrays.equals(m, to));

//        System.out.print("\033[31;1;4mIllegal move: " + letterize(from,to) + " - ");
//        if (piece.allyOf(getPiece(to))) System.out.print("cannot capture own piece.");
//        else System.out.print("no path.");
//        System.out.println("\033[0m");
    }

    public Board move(String move) {return move(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public Board move(String from, String to) {return move(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public Board move(int[] from, int[] to)
    {
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        board[to[1]][to[0]] = board[from[1]][from[0]]; // put moved piece to target location
        board[from[1]][from[0]] = VACANT.icon;         // erase moved piece from previous location
        board[8][0] = (char)to  [0]; board[8][1] = (char)to  [1];  // update metadata 'moved to'
        board[8][2] = (char)from[0]; board[8][3] = (char)from[1];  // update metadata 'moved from'
        board[8][4] = board[8][4] == 'w' ? 'b' : 'w';  // update identity of active turn

        // todo: update metadata

        return new Board(board);
    }

    @Override protected int hashIdentifier() {return hashcode;}
    @Override protected int evaluateFitness() {return score();}
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

    public static int[] normalize(char[] pos)
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
