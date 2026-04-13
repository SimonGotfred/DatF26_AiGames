package ai.game.demo.chess;

import agent.State;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static ai.game.demo.chess.Board.Position;

public class Piece extends State.Actionable<Board>
{
    public final Type    type;
    public final boolean color;
    Board.Position position;

    public char file(){return (char)(   position.x() +'a');} // letter notion
    public char rank(){return (char)((7-position.y())+'1');} // number notion
    public char x(){return position.x();}
    public char y(){return position.y();}

    public Piece(char type, Board board, String pos) {this(type,board,(char)(pos.charAt(0)-'a'),(char)(pos.charAt(1)-'1'));}
    public Piece(char type, Position pos)
    {
        this.position = pos;
        this.color = type < '♚';
        this.type  = Type.fromChar(type);
    }
//    public Piece(char type, Board table, int... pos) {this(type, new Position(table, pos));}
    public Piece(char type, Board board, char... pos)
    {
        this(type, new Position(board, pos));
    }

    public String name()  {return type.name();}
    public char   icon()  {return this.color ? type.icon : (char)(type.icon - 6);}
    public String color() {return type.icon == ' ' ? "blank" : this.color ? "black" : "white";}
    public int    value() {return color ? type.value : -type.value;}

    public boolean isWhite(){return !color;}
    public boolean isBlack(){return  color;}
    public boolean allyOf(Piece piece) {return this.value() * piece.value() > 0;}
    public boolean  foeOf(Piece piece) {return this.value() * piece.value() < 0;}

    public String position()      {return ""+file()+rank();}
    public Stream<char[]> moves() {return this.type.movesFrom(position).filter(pos-> !allyOf(position.board().getPiece(pos)));}

    public int compareTo(Piece other) {return this.value() - other.value();}
    public String toString() {return icon() + position();}

    @Override
    public Set<State.Action<Board>> actions()
    {
        Set<State.Action<Board>> actions = new HashSet<>();
        for (char[] move : moves().toList())
        {
            actions.add(new State.Action<>(position.board())
            {
                @Override
                public Board apply(Board board)
                {
                    return new Board(board.move(position.position(),move));
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
