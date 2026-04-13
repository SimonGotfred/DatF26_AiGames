package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.util.*;
import java.util.function.Predicate;

import static ai.game.demo.chess.Type.*;

public class Board extends State<Board> implements Comparable<Board>
{
    public record Dto(char[][] board){};
    public Dto toDto() {return new Dto(board);}

    public record Position(Board board, char... position)
    {
        public char x() {return dim(0);}
        public char y() {return dim(1);}
        public char z() {return dim(2);}
        public char         at (char[]  pos) {return board.at(pos);}
        public boolean whiteAt () {return board.whiteAt(position);}
        public boolean blackAt () {return board.blackAt(position);}
        public boolean pieceAt () {return board.pieceAt(position);}
        public boolean whiteAt (char... pos) {return board.whiteAt(pos);}
        public boolean blackAt (char... pos) {return board.blackAt(pos);}
        public boolean pieceAt (char... pos) {return board.pieceAt(pos);}
        public int      riskAt (char... pos) {return board. riskAt(pos);}
        public int      risk   ()            {return board. riskAt(position);}

        public char dim(int i) {try {return position[i];}catch(IndexOutOfBoundsException e){return 0;}}

        public String toString(){return board.getPiece(position).toString();}
    }

    private final char[][] board;

    public Board(char[][] board) {this.board = board;}
    public Board(String[] board)
    {
        this.board = new char[board.length][];
        for (int i = 0; i < board.length; i++) this.board[i] = board[i].toCharArray();
    }
    public Board()
    {
        this(new String[]
        {
            "♖♘♗♕♔♗♘♖",
            "♙♙♙♙♙♙♙♙",
            "        ",
            "        ",
            "        ",
            "        ",
            "♟♟♟♟♟♟♟♟",
            "♜♞♝♛♚♝♞♜",
        });
    }

    public char[][] raw() {return Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);}
    public Position getPosition(char... pos) {return new Position(this, pos);}
    public Piece    getPiece   (char... pos) {return new Piece(at(pos), this, pos);}
    public boolean  whiteAt    (char... pos) {return Type.isWhite(at(pos));}
    public boolean  blackAt    (char... pos) {return Type.isBlack(at(pos));}
    public boolean  pieceAt    (char... pos) {return Type.isPiece(at(pos));}
    public char     at         (char... pos)
    {
        try   {return board[pos[1]][pos[0]];}
        catch (ArrayIndexOutOfBoundsException e) {return ' ';}
    }
    protected char  set        (char  piece, char...pos)
    {
        char past = board[pos[1]][pos[0]];
        board[pos[1]][pos[0]] = piece;
        return past;
    }
    protected char  set        (Piece piece, char...pos) {return set(piece.icon(),pos);}

    public  Set<Piece> whites(){return pieces(Type::isWhite);}
    public  Set<Piece> blacks(){return pieces(Type::isBlack);}
    public  Set<Piece> pieces(){return pieces(Type::isPiece);}
    private Set<Piece> pieces(Predicate<Character> condition)
    {
        Set<Piece> pieces = new HashSet<>();
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

    /*
    public Board invert() {return new Board(invert(board));}
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
     */ // deprecated

    public Board move(String move)
    {
        Board _new = new Board(move(move.split(",")[0].trim(), move.split(",")[1].trim()));

//        System.out.println("test");
//        System.out.println(this.hashCode() + " / " + this.hashIdentifier());
//        System.out.println(this.toPrint());
//        System.out.println(_new.hashCode() + " / " + _new.hashIdentifier());
//        System.out.println(_new.toPrint());
//        System.out.println("test");

        return addChild(_new);
    }
    public char[][] move(String from, String to) {return move(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public char[][] move(Position from, char[] to) {return move(from.position(),to);}
    public char[][] move(char[] from, char[] to)
    {
        if (pieceAt(to)) announceCapture(getPiece(from),getPiece(to));
        char[][] board = Arrays.stream(this.board).map(char[]::clone).toArray(char[][]::new);
        board[to[1]][to[0]] = board[from[1]][from[0]];
        board[from[1]][from[0]] = ' ';
        return board;
    }

    /*
    public Set<Board> explore(){return explore(0);}
    public Set<Board> explore(int depth) // todo: tactic
    {
        try {if (Files.getFileStore(Path.of("C:")).getUsableSpace()>>30<3) return children;}
        catch (IOException e) {return children;}
        HashMap<Stream<char[]>,Piece> legal = depth%2!=0 ? whiteMoves() : blackMoves();
//        HashMap<Board,Piece> boards = new HashMap<>(); // for debugging
        for (Stream<char[]> moveSet : legal.keySet())
        {
            moveSet.forEach(move ->  addChild(new Board(move(legal.get(moveSet).position,move))));
//            {
//                Board b = new Board(move(legal.get(moveSet).position,move)); // for debugging
//                addChild(b);
//                boards.put(b,legal.get(moveSet));
//            });
        }
//        for (Board b : boards.keySet()) // for debugging
//        {
//            System.out.println(boards.get(b));
//            System.out.println(b);
//            System.out.println();
//        }
        if (depth < 1) return children;
        ConcurrentSkipListSet<Board> granChildren = new ConcurrentSkipListSet<>();
        for (Board child : children) {granChildren.addAll(child.explore(depth-1));}
        return granChildren;
    }
     */ // deprecated

    /*
    public HashMap<Stream<char[]>,Piece> moves() {return alternator() ? whiteMoves() : blackMoves();}
    public HashMap<Stream<char[]>,Piece> whiteMoves()
    {
        HashMap<Stream<char[]>,Piece> moves = new HashMap<>();
        whites().forEach(piece -> moves.put(piece.moves(), piece));
        return moves;
    }

    public HashMap<Stream<char[]>,Piece> blackMoves()
    {
        HashMap<Stream<char[]>,Piece> moves = new HashMap<>();
        blacks().forEach(piece -> moves.put(piece.moves(), piece));
        return moves;
    }

     */ // deprecated

    private static final Type[] simple =  new Type[]{KNIGHT, BISHOP, ROOK, QUEEN, KING};
    public int riskAt(Position position) {return position.risk();}
    public int riskAt(char...  position) // sum of pieces threatening the location, by using their patterns reversed
    {
        if (!(position[0] < 8 && position[1] < 8)) return 0; // skip non-pathable positions

        int sum = 0;
        Position pos = new Position(this,position);
        for (Type type : simple) // pattern for black/white pieces are mostly identical, so only
        {                        //  run each pattern once, collecting both corresponding black/white
            for (char[] p : type.movesFrom(pos).filter(p -> Type.fromChar(at(p)) == type).toList())
            {
                sum += Type.value(at(p)); // not just 'type', as *opposing* pieces of same type are also relevant
            }
        }

        for (int i : new int[]{-1,1}) // own logic for pawns as they move differently when capturing
        {
            if (at((char)(position[0]+i),(char)(position[1]+1)) ==  PAWN.icon  ) sum += 1;
            if (at((char)(position[0]+i),(char)(position[1]+1)) ==  PAWN.icon-6) sum -= 1;
        }
        return sum;
    }

    public boolean isLegalMove(String move) {return move.split(",").length == 2 && isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public boolean isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public boolean isLegalMove(char[] from, char[] to)
    {
        Piece piece = getPiece(from);
        List<char[]> moves = piece.moves().toList();

        if (moves.stream().anyMatch(m -> Arrays.equals(m, to))) return true;
        System.out.print("\033[31;1;4mIllegal move: " + letterize(from,to) + " - ");
        if (piece.allyOf(getPiece(to))) System.out.print("cannot capture own piece.");
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
        return Type.fromChar(at(from)).name()+' '+letterize(from)+" to "+letterize(to);
    }

    public void announceCapture(Piece taker, Piece taken)
    {
        System.out.println("\033[33;3m" + taker.color() + " " + taker.name() + " \tcaptures " + taken.color() + " " + taken.name() + "\033[0m");
    }

    public String toString() // aligns nicely in Obsidian
    {
        StringJoiner joiner = new StringJoiner("");
        String square = "░";
        String space  = "     ";

        joiner.add("```\n");
        joiner.add("       0  1  2  3  4  5  6  7\n");

        for (int i = 0; i < board.length; i++)
        {
            joiner.add((board.length-i)+" ");
            for (int j = 0; j < board[i].length; j++)
            {
                if ((i+j) % 2 != 0) square = "░░";
                else                square = "    ";

                if (Type.isPiece(board[i][j])) joiner.add(space + board[i][j] + space);
                else joiner.add(square);
            }
            joiner.add(" "+i+"\n");
        }

        joiner.add("       a  b  c  d  e  f  g  h");
        joiner.add("\n```");

        return joiner.toString();
    }

    public String toPrint() // aligns nicely in console
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

                if (Type.isPiece(board[i][j])) joiner.add(" " + board[i][j]);
                else joiner.add(square);
            }
            joiner.add(" "+i+"\n");
        }

        joiner.add("    a   b   c   d   e   f   g   h");

        return joiner.toString();
    }

    public String toSimpleString() // simplified String to use for hashCode
    {
        StringJoiner joiner = new StringJoiner("\n");
        for (char[] s : board) joiner.add(String.valueOf(s));
        return joiner.toString();
    }

    @Override protected int hashIdentifier() {return toSimpleString().hashCode();}
    @Override protected int evaluateFitness() {return score();}

    @Override
    public Set<Actionable<Board>> getActionables()
    {
        return new HashSet<>(alternator() ? whites() : blacks());
    }
}
