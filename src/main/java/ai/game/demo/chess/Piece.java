package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static ai.game.demo.chess.Board.Position;

public class Piece extends State.Actionable<Board>
{
    public final Type    type;
    public final boolean color;
    Board.Position position;

    public char file(){return (char)(   position.x() +'a');} // letter notion
    public char rank(){return (char)((7-position.y())+'1');} // number notion
    public int x(){return position.x();}
    public int y(){return position.y();}

    public Piece(char type, Board board, String pos) {this(type,board,(char)(pos.charAt(0)-'a'),(char)(pos.charAt(1)-'1'));}
    public Piece(char type, Position pos)
    {
        this.position = pos;
        this.color = type < '♚';
        this.type  = Type.fromChar(type);
    }
    public Piece(char type, Board board, int... pos) {this(type, new Position(board, pos));}

    public String name()  {return type.name();}
    public char   icon()  {return this.color ? (char)(type.icon - 6) : type.icon;}
    public String color() {return type.icon == ' ' ? "blank" : this.color ? "black" : "white";}
    public int    value() {return color ? type.value : -type.value;}

    public boolean isWhite(){return !color;}
    public boolean isBlack(){return  color;}
    public boolean allyOf(Piece piece) {return this.value() * piece.value() > 0;}
    public boolean  foeOf(Piece piece) {return this.value() * piece.value() < 0;}

    public String position()      {return ""+file()+rank();}
    public Stream<int[]> moves() {return this.type.movesFrom(position).filter(pos -> !allyOf(position.board().getPiece(pos)));}

    public int compareTo(Piece other) {return this.value() - other.value();}
    public String toString() {return color() + icon() + position();}

    @Override
    public Set<State.Action<Board>> actions()
    {
        Set<State.Action<Board>> actions = new HashSet<>();
        for (int[] move : moves().toList())
        {
            actions.add(new State.Action<>(position.board())
            {
                @Override public Board apply(Board board) {return board.move(position.position(),move);}
                @Override public int compareTo(State.Action<Board> other) {return 0;} // todo: weigh moves by heuristics
            });
        }
        return actions;
    }

    @Override
    public int compareTo(State.Actionable<Board> other) {return 0;} // todo: weigh pieces by heuristics
}
