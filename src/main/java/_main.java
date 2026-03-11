import board.*;
import agent.*;

public class _main
{
    public static void main(String[] args)
    {
        Board board = new Board(new String[]
            {
                    "     █    █",
                    " █ █ ██ █  ",
                    "  ◈██    ██",
                    " █ █  ██   ",
                    " █   █  ██ ",
                    " █ ███ █ █ ",
                    "         █◇",
            });

        long time = System.currentTimeMillis();

        Crawler crawler = new Crawler();
        int[][] path = crawler.crawl(board);

        System.out.println(System.currentTimeMillis()-time);

        System.out.println(board.toString(path));
    }
}
