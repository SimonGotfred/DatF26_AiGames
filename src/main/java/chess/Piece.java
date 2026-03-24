package chess;

import java.util.stream.Stream;

public class Piece implements Comparable<Piece>
{
    public Board board;
    public final Type    type;
    public final boolean color;
    public final char[]  position = new char[2];

    public char file(){return (char)(position[0]+'a');}
    public char rank(){return (char)(position[1]+'1');}
    public char x(){return position[0];}
    public char y(){return position[1];}

    public Piece(char type, Board board, int rank, int file)
    {
        this.board = board;
        this.color = type < '♚';
        this.type  = Type.fromChar(type);
        this.position[0] = (char)(file); // letter notion
        this.position[1] = (char)(rank); // number notion

//        if(color) type += 6;
//        this.type   = Type.values()[5-(type-'♚')];
    }

    public Piece(char type) {this(type,null,0,0);}
    public Piece(char type, Board board, char rank, char file)
    {
        this(type,board,rank-'1',file-'a');
    }

    public String name()  {return type.name();}
    public char   icon()  {return this.color ? type.icon : (char)(type.icon - 6);}
    public String color() {return this.color ? "black" : "white";}
    public int    value() {return type.value;}

    public boolean isWhite(){return !color;}
    public boolean isBlack(){return  color;}

    public String position()      {return ""+file()+rank();}
    public Stream<char[]> moves() {return this.type.movesFor(this);}

    @Override
    public int compareTo(Piece other) {return this.value() - other.value();}
    public String toString() {return icon() + position();}
}
