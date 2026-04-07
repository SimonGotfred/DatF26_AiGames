
import chess.*;
import util.NodeMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class _main
{
    public static void main(String[] args) throws IOException
    {
        // just printing values of chars for debug reference
//        for (char c:Type.black.toCharArray()) {System.out.println(c + ": " + ((int)c));}
//        for (char c:Type.white.toCharArray()) {System.out.println(c + ": " + ((int)c));}

        // todo: Agent<Board> agent = new Agent<>();

        Board board = new Board();
        board.output();
//        play(board);

//        explore(board,0);
//        truncate(10);
//        NodeMap.output(Board.class);

        System.out.println("Space: " + (Files.getFileStore(Path.of("C:")).getUsableSpace()>>30));
        System.out.println("Goodbye");
    }

    private static void play(Board board) throws IOException
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String move = "";
        while (!move.equals("x"))
        {
            System.out.println(board);

            System.out.println("Please enter next move:");
            move = r.readLine();

            System.out.println();
            if (board.isLegalMove(move)) board = board.move(move);

            // todo: board = Agent.act(board);
        }
    }

    private static void explore(Board board, int depth)
    {
        int size = 0;
        LocalDateTime now, then = LocalDateTime.now();
        try {board.evaluate(0);}
        catch (OutOfMemoryError e)
        {
            System.out.println("*snap*");
            System.out.println(e.getMessage());
            size = NodeMap.size(Board.class);
            board.clear(); NodeMap.clear(Board.class);
        }
        now = LocalDateTime.now();
        size = size!=0 ? size : NodeMap.size(Board.class);
        LocalTime t = LocalTime.MIN.plus(Duration.between(then, now));

        System.out.println(size-1 + " states in");
        System.out.println(DateTimeFormatter.ISO_TIME.format(t));
    }

    private static int truncate(int limit)
    {
        while (NodeMap.size(Board.class) > limit)
        {
            NodeMap.of(Board.class).pollFirstEntry().getValue().remove();
        }
        System.out.println("Truncated to " + NodeMap.size(Board.class));
        return NodeMap.size(Board.class);
    }
}
