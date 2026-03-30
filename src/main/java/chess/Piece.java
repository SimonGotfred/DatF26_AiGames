package chess;

import agent.State;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Piece extends State.Actionable<Board>
{
    public final Type    type;
    public final Board   board;
    public final boolean color;
    public final char[]  position = new char[2];

    public char file(){return (char)(   position[0] +'a');}
    public char rank(){return (char)((7-position[1])+'1');}
    public char x(){return position[0];}
    public char y(){return position[1];}

    public Piece(char type, Board board, int file, int rank)
    {
        this.board = board;
        this.color = type < '♚';
        this.type  = Type.fromChar(type);
        this.position[0] = (char) (file); // letter notion
        this.position[1] = (char) (rank); // number notion
    }

    public Piece(char type) {this(type,null,0,0);}
    public Piece(char type, Board board, char file, char rank) {this(type,board,rank-'1',file-'a');}

    public Piece(char type, Board board, String pos) {this(type,board,pos.toCharArray());}
    public Piece(char type, Board board, char... pos) {this(type,board,pos[0]-'a',pos[1]-'1');}

    public String name()  {return type.name();}
    public char   icon()  {return this.color ? type.icon : (char)(type.icon - 6);}
    public String color() {return type.icon == ' ' ? "blank" : this.color ? "black" : "white";}
    public int    value() {return color ? type.value : -type.value;}

    public boolean isWhite(){return !color;}
    public boolean isBlack(){return  color;}
    public boolean onTeam (Piece piece) {return this.value() * piece.value() > 0;}
    public boolean against(Piece piece) {return this.value() * piece.value() < 0;}

    public String position()      {return ""+file()+rank();}
    public Stream<char[]> moves() {return this.type.movesFor(this).filter(pos-> !onTeam(board.getPiece(pos)));}

    public int compareTo(Piece other) {return this.value() - other.value();}
    public String toString() {return icon() + position();}

    @Override
    public Set<State.Action<Board>> actions()
    {
        Set<State.Action<Board>> actions = new HashSet<>();
        for (char[] move : moves().toList())
        {
            actions.add(new State.Action<>(board)
            {
                @Override
                public Board apply(Board board)
                {
                    return new Board(board.move(position,move));
                }
                @Override
                public int compareTo(State.Action<Board> o)
                {
                    return 0; // todo: weigh moves for given piece
                }
            });
        }
        return actions;
    }
}
