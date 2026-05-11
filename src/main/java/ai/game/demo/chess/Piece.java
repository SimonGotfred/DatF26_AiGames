package ai.game.demo.chess;

import ai.game.demo.agent.State;

import java.awt.*;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Piece extends State.Actionable<Board>
{
    public final Board    board;
    public final Type      type;
    public final boolean  color;
    public final int[] position;

    public char file(){return (char)(   position[0] +'a');} // letter notion
    public char rank(){return (char)((7-position[1])+'1');} // number notion
    public int x(){return position[0];}
    public int y(){return position[1];}

    public Piece(char type, Board board, String pos) {this(type,board,(char)(pos.charAt(0)-'a'),(char)(pos.charAt(1)-'1'));}
    public Piece(char type, Board board, int... pos)
    {
        this.board = board;
        this.position = pos;
        this.type  = Type.fromChar(type);
        this.color = this.type.color==Color.BLACK;
    }

    public String name()  {return type.name();}
    public char   icon()  {return this.color ? (char)(type.icon - 6) : type.icon;}
    public String color() {return type.icon == ' ' ? "blank" : this.color ? "black" : "white";}
    public int    value() {return color ? type.value : -type.value;}

    public boolean isWhite(){return !color;}
    public boolean isBlack(){return  color;}
    public boolean allyOf(Piece piece) {return this.value() * piece.value() > 0;}
    public boolean  foeOf(Piece piece) {return this.value() * piece.value() < 0;}

    public String position()      {return ""+file()+rank();}
    public Stream<int[]> moves() {return this.type.movesFrom(board,position).filter(pos -> type.color!=Type.color(board.at(pos)));}

    public int compareTo(Piece other) {return this.value() - other.value();}
    public String toString() {return color() + type.icon + position();}

    @Override
    public TreeSet<State.Action<Board>> actions()
    {
        TreeSet<State.Action<Board>> actions = new TreeSet<>();
        for (int[] move : moves().toList())
        {
            actions.add(new State.Action<>(board)
            {
                @Override public Board apply(Board board) {return board.move(position,move);}
                @Override public int evaluateFitness()
                {
                    return Type.value(board.at(move))+type.valueAt(move)+board.riskAt(move);
                }
            });
        }
        return actions;
    }

    @Override
    public int compareTo(State.Actionable<Board> other) {return 0;} // todo: weigh pieces by heuristics
}
