package chess;

import java.util.stream.Stream;

public class Piece
{
    public Board board;
    public final Type    type;
    public final boolean color;
    public final char[]  position = new char[2];

    public char rank(){return (char)(position[1]+'1');}
    public char file(){return (char)(position[0]+'a');}
    public int y(){return position[1];}
    public int x(){return position[0];}

    public Piece(char type, int rank, int file, Board board)
    {
        this.board = board;
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

    public int  value() {return type.value;}
    public char icon() {return this.color ? type.icon : (char)(type.icon - 6);}

    public String position() {return ""+file()+rank();}
    public Stream<char[]> moves()
    {
        Stream<char[]> moves = this.type.movesFor(this);

        return moves.filter(pos -> !board.whiteAt(pos));
    }
}
