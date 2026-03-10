public class _main
{
    public static void main(String[] args)
    {
        Board board = new Board(new String[]
            {
                    "     █    █",
                    " █ █ ██ █  ",
                    "  ⧪██    ██",
                    " █ █  ██   ",
                    " █   █  ██ ",
                    " █ ███ █ █ ",
                    "         █⚑",
            });

        Crawler crawler = new Crawler();
        int[][] path = crawler.crawl(board);
        System.out.println(board.toString(path));
    }
}
