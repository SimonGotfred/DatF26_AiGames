package agent;

import board.Board;

import java.util.Arrays;
import java.util.HashSet;

public class Crawler extends Thread
{
    private   static Board board;
    private   static final HashSet<int[]>    list2    = new HashSet<>();
    private   static final HashSet<Crawler>  crawlers = new HashSet<>();
    protected static synchronized void print   (int[] pos) {System.out.println(board.toString(pos));}
    protected static synchronized void printAll(Crawler c){crawlers.add(c); list2.add(c.position); printAll();}
    public    static synchronized void printAll()
    {
        System.out.println("crawlers~ " + crawlers.size());
        System.out.println(board.toString(list2.toArray(new int[list2.size()][])));
    }

    private   static final   HashSet<String> list = new HashSet<>();
    protected static boolean add(int[] pos) {return list.add(Arrays.toString(pos));}

    protected int       id;
    private   int[]     position;
    private   int[][]   path;
    protected Crawler   parent;
    protected Crawler[] children;
    private Crawler(Crawler parent, int id){this.parent=parent; this.id = id;}
    public  Crawler(){}

    public    int[][] crawl(Board board) {return crawl(board, board.start(), board.end());}
    public    int[][] crawl(Board board, int[] from, int[] to)
    {
        Crawler.board = board;
        Crawler crawler = new Crawler()
        {
            @Override public void run()
            {
                this.crawl(board, board.start(), board.end(), new int[2]);
            }
        };
        try {crawler.start(); crawler.join();}
        catch (InterruptedException _) {}
        return crawler.path;
    }
    protected int[][] crawl(Board board, int[] from, int[] to, int[] last)
    {
        position = from;
        if (board.pos(from) == Board.GOAL)
            return new int[][]{from}; // return successful conclusion

        children = new Crawler[4];
        int i = 0;
        for (int[] pos : adjacent(from)) // initialize adjacent sub-crawlers
        {
            if (board.pos(pos)==Board.WALL || !add(pos)) continue; // except on walls or already processed pos
            children[i] = new Crawler(this, i)
            {
                @Override public void run()
                {
//                    new Thread(()->Crawler.printAll(pos,this)).start();
                    this.parent.setPath(this.crawl(board, pos, to, from)); // hand path conclusion back to parent
                    this.conclude();
                }
            };
            children[i].start();
            i++;
        }
        return wait(children); // await subs' crawling to conclude
    }

    private synchronized int[][] wait(Crawler[] children)
    {
        while (!done())
        {
            try   {this.wait();}
            catch (InterruptedException _) {}
        }
        killChain(); // kill children when done
        return path; // at this point, either a child has set a path leading to the goal, or this point doesn't lead towards the goal.
    }

    protected synchronized boolean done()
    {
        if (path != null) return true; // declare self done if it has path or all children are done
        return children[0] == null
             & children[1] == null
             & children[2] == null
             & children[3] == null;
    }

    protected synchronized void setPath(int[][] path)
    {
        if (path == null || this.path != null) return;
        this.path = Arrays.copyOf(path,path.length+1);
        this.path[path.length] = position;
//        print(position);
    }

    private void killChain()
    {
        for (Crawler c : children)
        {
            if (c != null) c.interrupt(); // shut down still-active children
        }
        children=null;
    }

    protected void conclude()
    {
        this.parent.children[this.id] = null; // remove sub from kept memory
        if (this.parent.done()) this.parent.interrupt(); // notify parent that *all* children are done
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
