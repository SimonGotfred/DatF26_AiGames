import java.util.Arrays;

public class Board implements Comparable<Board>
{
    public static final char
     START = '⧪',
     WALL  = '█',
     HARD  = '░',
     OPEN  = ' ',
     GOAL  = '⚑',
     STEP  = '*';

    private final String[] board;

    public Board(){this(new String[0]);}
    public Board(String[] board) {this.board = board;}

    public int length() {return board.length;}
    public int width () {return board.length;}

    public String row(int row)
    {
        try{return board[row];}
        catch(IndexOutOfBoundsException e){return "";}
    }
    public String col(int col)
    {
        StringBuilder builder = new StringBuilder();

        for (String s : board)
        {
            try{builder.append(s.charAt(col));}
            catch(IndexOutOfBoundsException e){builder.append(WALL);}
        }

        return builder.toString();
    }
    public char   pos(int[] pos) {return pos(pos[0],pos[1]);}
    public char   pos(int row, int col)
    {
        try{return board[row].charAt(col);}
        catch(IndexOutOfBoundsException e){return WALL;}
    }

    public int[] start() {return find(START);}
    public int[] end()   {return find(GOAL);}
    public int[] find(char c)
    {
        int i = 0;
        for (String s : board)
        {
            if (s.indexOf(c)>-1) return new int[]{i, s.indexOf(c)};
            i++;
        }
        return null;
    }

    public int compareTo(Board that)
    {
        if (this.length() != that.length()) return this.length() - that.length();
        int i = 0;
        for (String row : board)
        {
            if (!row.equals(that.row(i)))
            {
                return row.compareTo(that.row(i));
            }
            i++;
        }
        return 0;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("\n");
        for (String s : board)
        {
            try{builder.append(s).append('\n');}
            catch(IndexOutOfBoundsException e){builder.append(WALL);}
        }
        return builder.toString();
    }

    public String toString(int[] ... path)
    {
        if (path == null) return toString();
        String[] pathed = Arrays.copyOf(board,board.length);
        StringBuilder builder;
        for (int[] pos : path)
        {
            builder = new StringBuilder(pathed[pos[0]]);
            if (path.length==1 || builder.charAt(pos[1]) != START && builder.charAt(pos[1]) != GOAL)
                builder.deleteCharAt(pos[1]).insert(pos[1],STEP);
            pathed[pos[0]] = builder.toString();
        }
        return new Board(pathed).toString();
    }
}
