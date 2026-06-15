package ai.game.demo.chess;

import ai.game.demo.agent.State;
import lombok.Getter;

import java.awt.*;
import java.util.TreeSet;
import java.util.stream.Stream;

@Getter
public class Piece extends State.Actionable<Board>
{
    public final Board    board;
    public final Type      type;
    public final Color    color;
    public final int[] position;

    public char file(){return (char)(   position[0] +'a');} // letter notion
    public char rank(){return (char)((7-position[1])+'1');} // number notion

    public Piece(Type type, Board board, int... pos)
    {
        this.board    = board;
        this.position = pos;
        this.type     = type;
        this.color    = type.color;
    }

    public String name()  {return type.name();}
    public String color() {return type.icon == ' ' ? "blank" : isBlack() ? "black" : "white";}
    public int    value() {return type.value;}

    public boolean isBlack(){return color==Color.BLACK;}

    public String position()      {return ""+file()+rank();}
    public Stream<int[]> moves() {return this.type.movesFrom(board,position).filter(pos -> type.color!=board.at(pos).color);}

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
                    return type.valueOf(move)+board.riskAt(move)-(board.at(move).value);
                } // subtract taken piece's value as it's value is negative to moves purpose
            });
        }
        return actions;
    }

    @Override
    public int compareTo(State.Actionable<Board> other) {return 0;} // todo: weigh pieces by heuristics
}
