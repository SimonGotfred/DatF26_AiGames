package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.game.demo.chess.Type.*;
import static ai.game.demo.util.Direction.*;
import static java.awt.Color.WHITE;
import static java.awt.Color.BLACK;

public class Board extends State<Board> implements Comparable<Board>
{
    public static Board test()
    {
        return new Board
        (
            "♖♘ㅤㅤㅤ♗♘♖" +
            "♙♙♟♙♙ㅤ♙♙" +
            "ㅤㅤㅤ♕ㅤㅤ♖ㅤ" +
            "ㅤ♟♗ㅤㅤㅤㅤㅤ" +
            "ㅤㅤㅤ♙ㅤ♔ㅤ♜" +
            "ㅤ♞♝♛ㅤㅤㅤㅤ" +
            "♟ㅤ♟♟♟♟♟♟" +
            "♜ㅤㅤㅤ♚ㅤㅤ♜"
        );
    }

    public static final int[] fields;
    public static final int[][] map = new int[8*8*2][];

    private static Type get(Type[][] board, int   pos) {return get(board,map[pos&255]);}
    private static Type get(Type[][] board, int[] pos) {return board[pos[1]][pos[0]];}
    private static void set(Type[][] board, int   pos, Type piece) {set(board,map[pos&255],piece);}
    private static void set(Type[][] board, int[] pos, Type piece) {board[pos[1]][pos[0]]=piece;}

    private static int flags=0;
    private static final String[] initialFlags= new String[]{"tfwppcccccc"}; // ! yes, there is a reason for this
    // being an array
    public  static final int TO, FROM, TURN, PROMOTION, PASSANT,
                             CASTLE_BLACK, CASTLE_BLACK_LEFT, CASTLE_BLACK_RIGHT,
                             CASTLE_WHITE, CASTLE_WHITE_LEFT, CASTLE_WHITE_RIGHT;

    static // set flag indexes
    {
        TO=flags++;
        FROM=flags++;
        TURN=flags++;
        PROMOTION=flags++;
        PASSANT=flags++;
        CASTLE_BLACK =flags++;
        CASTLE_BLACK_LEFT =flags++;
        CASTLE_BLACK_RIGHT =flags++;
        CASTLE_WHITE =flags++;
        CASTLE_WHITE_LEFT =flags++;
        CASTLE_WHITE_RIGHT =flags++;

        List<Integer> temp = new ArrayList<>();

        int i = 0;
        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                temp.add(i);
                map[i++]=new int[]{col,row};
            }
            i+=8;
        }
        fields = temp.stream().mapToInt(value -> value).toArray();
    }

    public record Dto(Type[][] board){};
    public Dto toDto() {return new Dto(board);}

    private final int hashcode;
    private final Type [][] board;
    private final char [] metadata;
    public  final int[] checks;

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
        this.hashcode=nef().hashCode();
        this.checks  =checks();
    }
    public Board(Type[][] board) {this(board,initialFlags[0].toCharArray());}
    public Board(Type[][] board,char[] meta) {this.board=board;this.metadata=meta;this.hashcode=nef().hashCode();this.checks = checks();}
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
        this.hashcode = nef().hashCode();
        this.checks   = checks();
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

    public char[][] raw() {return Stream.of(nef().substring(0,64).split("(?<=\\G........)"), new String[]{String.valueOf(metadata)}).flatMap(Stream::of).map(String::toCharArray).toArray(char[][]::new);}
    public char flag(int index){return metadata[index];}
    
    public Piece    getPiece   (int...  pos) {return new Piece(at(pos), this, pos);}
    public boolean  whiteAt    (int...  pos) {return at(pos).isWhite(   );}
    public boolean  blackAt    (int...  pos) {return at(pos).isBlack(   );}
    public boolean  pieceAt    (int     pos) {try{return pieceAt(map[pos & 255]);}catch (ArrayIndexOutOfBoundsException e){return false;}}
    public boolean  pieceAt    (int...  pos) {return at(pos).isPiece(   );}
    public int      valueAt    (int...  pos) {return at(pos).valueOf(pos);}
    public Type     at         (int     pos) {try{return at(map[pos&255]);}catch (ArrayIndexOutOfBoundsException e) {return VACANT;}}
    public Type     at         (int...  pos) {try{return board[pos[1]][pos[0]];}catch (ArrayIndexOutOfBoundsException e) {return VACANT;}}
    public Type     at         (String  pos) {return at(normalize(pos.toCharArray()));}

    public boolean maximize(){return metadata[TURN]=='w';}
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
                if (condition.test(c.icon)) pieces.add(new Piece(c, this, file, rank));
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

    public boolean isCheck(){return isCheck(turn());}
    public boolean isCheck(Color color){return isCheck(color,king(color));}

    public static char[][] invert(char[][] board) // ! deprecated
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

    private static final Type[][] simple =  new Type[][]{new Type[]{BISHOP, ROOK},new Type[]{KNIGHT}};
    public int riskAt(int position){return threats(position).stream().mapToInt(Piece::value).sum();} // returns sum of potential trade-chain at given location
    public List<Piece> threats(int position) // list of pieces threatening the location, by using their patterns reversed
    {
        List<Piece> pieces = new ArrayList<>();
        for (Type type : simple[0]) // pattern for black/white pieces are mostly identical, so only
        {                        //  run each pattern once, collecting both corresponding black/white
            for (int p : type.movesUnchecked(this,position).filter(p->at(p).type()==type||at(p).type()==QUEEN).toList())
            {
                pieces.add(new Piece(at(p),this, p));
            }
        }
        for (Type type : simple[1]) // pattern for black/white pieces are mostly identical, so only
        {                        //  run each pattern once, collecting both corresponding black/white
            for (int p : type.movesUnchecked(this,position).filter(p->at(p).type()==type).toList())
            {
                pieces.add(new Piece(at(p),this, p));
            }
        }
        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                if(at(i,j).type()==KING&&!(i==0&&j==0)) pieces.add(new Piece(at(i,j),this, i,j));
            }
        }
        for (int i : Type.w_mirror()) // own logic for pawns as they move differently when capturing
        {
            if (at(position+i) == BLACK_PAWN) pieces.add(new Piece(BLACK_PAWN,this, position+i));
            if (at(position+i) == BLACK_PAWN) pieces.add(new Piece(BLACK_PAWN,this, position+i));
        }
        for (int i : Type.b_mirror())
        {
            if (at(position+i) == PAWN) pieces.add(new Piece(PAWN,this, position+i));
            if (at(position+i) == PAWN) pieces.add(new Piece(PAWN,this, position+i));
        }
        return pieces;
    }

    public Color turn(){return flag(TURN)=='w'?WHITE:BLACK;}
    public int king(){return king(turn());}
    public int king(Color color) // returns the position of the king by given color
    {
        for (int field : fields)
        {
            if (at(field).type()==KING&&at(field).color==color) return field;
        }
        return -1;
    } private static final int[] notFound = new int[]{-10,-10};

    public boolean isCheck(Color color, int position){return threats(position).stream().anyMatch(piece->piece.color!=color);}
    public int[] checks() // returns an array of coordinates that can be moved to, to intercept a threat to the king of the current turn
    {
        int[] threats = threats(king(turn())).stream().filter(piece -> piece.color!=turn()).mapToInt(Piece::getPosition).toArray(); // gather coordinates of pieces threatening the king
        if(threats.length>1) return multipleThreats; // signal *must* move king
        if(threats.length>0)
        {
            if(at(threats[0]).type()==KNIGHT) return threats; // knights can only be intercepted by capture
            List<Integer> path = new ArrayList<>();
            int king = king(turn());
            int threat = threats[0];
            int i = map[threat][0]==map[king][0]?0:map[threat][0]> map[king][0]?SOUTH.x88:NORTH.x88;
            int j = map[threat][1]==map[king][1]?0:map[threat][1]> map[king][1]?WEST.x88:EAST.x88;
            while(!(map[threat][0]==map[king][0]&& map[threat][1]==map[king][1])) path.add(king+=i+j); // "draw" line from threat to king, collecting passed coordinates
            return path.stream().mapToInt(value -> value).toArray();
        }
        else return null; // returns null if no threats to make logic easier
    } private static final int[] multipleThreats = new int[0];

    // return stream of legal moves the piece at given coordinates can make
    public Stream<Integer> movesFor(int... position){return movesFor(position[0]+(position[1]<<4));}
    public Stream<Integer> movesFor(int position){return at(position).isTurn(flag(TURN))
                                                        ? at(position).movesFrom(this, position)
                                                        : Stream.empty();}

    // check if given move (e.g. a2,b3) is legal
    public int[] isLegalMove(String move) {return move.split(",").length == 2
                                                ? isLegalMove(move.split(",")[0].trim(), move.split(",")[1].trim())
                                                : null;}
    public int[] isLegalMove(String from, String to) {return isLegalMove(normalize(from.toCharArray()),normalize(to.toCharArray()));}
    public int[] isLegalMove(int[] from, int[] to)
    {
        if(at(from).color==at(to).color) return null;
        Type piece = at(from);
        return map[movesFor(from).filter(m -> at(m).color != piece.color)
                             .filter(m -> Arrays.equals(map[m&255],to))
                             .findAny().orElse(-1)];
    }

    public Board move(String move) {return move(move.split(",")[0].trim(), move.split(",")[1].trim());}
    public Board move(String from, String to) {return move(normalize(from.toCharArray()),isLegalMove(from+','+to));}
    public Board move(int[] from, int[] to){return move((from[0]+(from[1]<<4)),(to[0]+(to[1]<<4)));}
    public Board move(int from, int to)
    {
        Type[][] board = Arrays.stream(this.board).map(Type[]::clone).toArray(Type[][]::new);
//        int fromX = from[0];
//        int fromY = from[1];
//        int   toX =   to[0];
//        int   toY =   to[1];

        set(board,to,get(board,from)); // put moved piece to target location
        set(board,from,VACANT);;        //  erase moved piece from previous location

        char[] metadata = this.metadata.clone();
        metadata[  TO] = (char)to;  // update metadata 'moved to'
        metadata[FROM] = (char)from;  // update metadata 'moved from'
        metadata[TURN] = metadata[TURN] == 'w' ? 'b' : 'w';  // update identity of active turn

        castling(board,metadata,from,to); // apply castling rules

        //basic en passant logic :/
        if (at(from).isType(PAWN))
        {
            int passantTarget = -1;
            //take en passant target else set passantTarget
            if (metadata[PASSANT] == to) board[from>>4][to&7] = VACANT;
            else passantTarget = setPassant(from, to);
            metadata[PASSANT] = (char)passantTarget;
        }
        else
        {
            metadata[PASSANT] = 'p';
        }

        //promotion
        if(to>>8!=0)
        {
            set(board,to,(turn()==WHITE?Type.values()[to>>8]:Type.values()[to>>8].invert()));
        }

        return new Board(board,metadata);
    }

    public int setPassant(int from, int to)
    {
        //en passant availability check
        if (Math.abs((from>>4)-(to>>4)) == 2)
        {
            return from + (turn()==WHITE ? SOUTH.x88 : NORTH.x88);
        }

        return 'p';
    }

    public boolean passantAt(int passantPos) {return metadata[PASSANT]==passantPos;}

    private void castling(Type[][] board,char[] metadata, int from, int move)
    {
        if (at(from).type()==KING)
        {
            int king = turn() == WHITE ? CASTLE_WHITE : CASTLE_BLACK;
            if (metadata[king] != ' ') // castling
            {
                if ((move & 7) == 2)
                {
                    board[move >> 4][(move & 7) + 1] = board[move >> 4][0];
                    board[move >> 4][0] = VACANT;
                } // left
                else if ((move & 7) == 6)
                {
                    board[move >> 4][(move & 7) - 1] = board[move >> 4][7];
                    board[move >> 4][7] = VACANT;
                }// right
                metadata[king] = ' '; // erase king castling-flag
            }
        }

        // check if expected rook is present. the alternative would be to check *both* to or from for
        // if they match coordinates, to account for capture of unmoved rook, ie double the checks.
        // possibility of captured rook also means *both* white and black must be checked each turn
        if(metadata[CASTLE_BLACK]!=' ')
        {
            if (board[0][0] != BLACK_ROOK) metadata[CASTLE_BLACK_LEFT ] = ' ';
            if (board[0][7] != BLACK_ROOK) metadata[CASTLE_BLACK_RIGHT] = ' ';
        }
        if(metadata[CASTLE_WHITE]!=' ')
        {
            if (board[7][0] != ROOK) metadata[CASTLE_WHITE_LEFT ] = ' ';
            if (board[7][7] != ROOK) metadata[CASTLE_WHITE_RIGHT] = ' ';
        }
    }

    @Override protected int hashIdentifier (){return hashcode;}
    @Override protected int evaluateFitness(){return score ();}
    @Override
    public List<Actionable<Board>> getActionables(boolean isBlackTurn) {return isBlackTurn ? blacks() : whites();}

    @Override
    public TreeSet<Action<Board>> getActions(boolean isBlackTurn)
    {
        Color color = isBlackTurn ? BLACK : WHITE;
        TreeSet<Action<Board>> actions = new TreeSet<>();
        for (int position : fields)
        {
            if(at(position).color == color)
            {
                for (int move : at(position).movesFrom(this, position).toList())
                {
                    actions.add(new State.Action<>(this)
                    {
                        @Override public Board apply(Board board){return board.move(position,move);}
                        @Override public int evaluateFitness()   {return at(position).value+at(position).valueOf(move)+state.riskAt(move);}
                    });
                }
            }
        }

        return actions;
    }

    public String nef(){return Arrays.stream(Arrays.stream(this.board).map(row->Arrays.stream(row).map(Type::toString).collect(Collectors.joining()).toCharArray()).toArray(char[][]::new)).map(String::valueOf).collect(Collectors.joining())+String.valueOf(metadata);}

    public String toString() // simplified String to use for hashCode
    {
        StringJoiner joiner = new StringJoiner("\n");
//        joiner.add(""+metadata[4]);
        Arrays.stream(board).limit(8).forEach(row -> joiner.add(Arrays.stream(row).map(Type::toString).collect(Collectors.joining())));
//        for (Type[] s : board) joiner.add(Arrays.stream(s).map(Type::toString).collect(Collectors.joining()));
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
