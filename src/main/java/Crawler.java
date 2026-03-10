import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Crawler extends Thread
{
    private   static int t = 0;
    private   static HashSet<String> list = new HashSet<>();
    private   static HashSet<int[]> list2 = new HashSet<>();
    protected static synchronized boolean add(int[] pos)
    {
        boolean b = list.add(Arrays.toString(pos));
//        if (b)
//        {
//            list2.add(pos);
//            System.out.println(board.toString(list2.toArray(new int[list2.size()][])));
//        }
        return b;
    }
    protected static Board board;

    private   int       id;
    private   int[]     position;
    private   int[][]   path;
    protected Crawler   parent;
    protected Crawler[] children;
    private Crawler(Crawler parent, int id){this.parent=parent; this.id = id;}
    public  Crawler(){}

    public    int[][] crawl(Board board)                       {return crawl(board, board.start(), board.end());}
    public    int[][] crawl(Board board, int[] from, int[] to) {Crawler.board = board; return crawl(board, board.start(), board.end(), new int[2]);}
    protected int[][] crawl(Board board, int[] from, int[] to, int[] last)
    {
        position = from;
        if (board.pos(from) == Board.GOAL)
            return new int[][]{from}; // return successful conclusion

//        if (t > 20) return null;

        children = new Crawler[4];
        int i = 0;
        for (int[] pos : adjacent(from)) // initialize adjacent sub-crawlers, except backwards or on walls
        {
            if (board.pos(pos)==Board.WALL || Arrays.equals(pos,last) || !add(pos)) continue;
            children[i] = new Crawler(this, i)
            {
                @Override public void run()
                {
//                    path = crawl(board, pos, to, from); // hand path conclusion back to parent
                    this.parent.setPath(crawl(board, pos, to, from));
                    this.parent.children[id] = null;
                    t--;
//                    if (parent.done())
//                        parent.notify();
//                    parent.remove(id); // remove sub from kept memory
                }
            };
            children[i].start();
            i++; t++;
        }
        return wait(children); // await subs' crawling to conclude
    }

    private int[][] wait(Crawler[] children)
    {
        while (path == null)
        {
            if (children[0]==null
              & children[1]==null
              & children[2]==null
              & children[3]==null)
                break;
//            try {this.wait();}
//            catch (InterruptedException _)
//            {return path;}
        }
        if (path==null)
            return null;
        return path; // at this point, either a child has set this path leading to the goal, or this point doesn't lead towards the goal.
    }

    protected synchronized boolean done()
    {
        if (path != null) return false;
        return children[0] == null
             & children[1] == null
             & children[2] == null
             & children[3] == null;
    }

    protected synchronized void setPath(int[][] path)
    {
        if (path == null) return;
        this.path = Arrays.copyOf(path,path.length+1);
        this.path[path.length] = position;
    }

    private int[][] adjacent(int[] from)
    {
        return new int[][]
                {
                        {from[0]+1,from[1]},
                        {from[0]-1,from[1]},
                        {from[0],from[1]+1},
                        {from[0],from[1]-1}
                };
    }

    public void run() {}

    public String toString()
    {
        return board.toString(new int[][]{position});
    }
}
