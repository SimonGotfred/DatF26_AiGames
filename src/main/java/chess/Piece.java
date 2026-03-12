package chess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Piece
{
    static       Board board = new Board(new char[8][8]);

    public final Type    type;
    public final boolean color;
    public final char[]  position = new char[2];

    public char rank(){return (char)(position[1]+'1');}
    public char file(){return (char)(position[0]+'a');}
    public int y(){return position[1];}
    public int x(){return position[0];}

    public Piece(char type, int rank, int file)
    {
        color = type < '♚';
        if(color) type += 6;
        this.type   = Type.values()[type-'♚'];
        this.position[1] = (char)(rank); // number notion
        this.position[0] = (char)(file); // letter notion
    }

    public Piece(char type, char rank, char file)
    {
        color = type < '♚';
        if(color) type += 6;
        this.type   = Type.values()[type-'♚'];
        this.position[1] = (char)(rank-'1'); // number notion
        this.position[0] = (char)(file-'a'); // letter notion
    }

    public Piece(Type type, boolean color, char rank, char file)
    {
        this.color  = color;
        this.type   = type;
        this.position[1] = rank; // number notion
        this.position[0] = file; // letter notion
    }

    public char icon()
    {
        return this.color
               ? type.icon
               : (char)(type.icon - 6);
    }

    public String posistion() {return ""+file()+rank();}
    public Map<char[],Integer> moves()
    {
        Map<char[],Integer> moves = new HashMap<>();
        Set<char[]> move = this.type.movesFrom(position);

        move.removeIf(m -> board.stream()
              .filter(p -> p.color == this.color).anyMatch(p -> Arrays.equals(m, p.position)));

        return moves;
    }
}
