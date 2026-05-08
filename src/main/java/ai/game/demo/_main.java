package ai.game.demo;

import ai.game.demo.agent.Agent;
import ai.game.demo.chess.*;
import ai.game.demo.util.*;
import lombok.SneakyThrows;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class _main
{
    private static final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private static final FileStore store;
    static
    {
        try {store = Files.getFileStore(Path.of("C:"));}
        catch (IOException e) {throw new RuntimeException(e);}
    }


    public static void main(String[] args) throws IOException
    {
        // just printing values of chars for debug reference
//        for (char c:Type.black.toCharArray()) {System.out.println(c + ": " + ((int)c));}
//        for (char c:Type.white.toCharArray()) {System.out.println(c + ": " + ((int)c));}

        System.out.println("usable space: "+(store.getUsableSpace()>>30)+" GB");

        Board board = new Board();
        Agent<Board> agent = new Agent<>(board);
        play(agent);

//        board = board.minMax(2);
//        System.out.println(board.toPrint());
//        explore(board,0);
//        System.out.println(NodeMap.size(Board.class));
//        truncate(10);
//        NodeMap.output(Board.class);
//        System.out.println("Space: " + (Files.getFileStore(Path.of("C:")).getUsableSpace()>>30));

        System.out.println("goodbye");
    }

    private static void play(Agent<Board> agent) throws IOException
    {
        String move = "";
        Board board = agent.getCurrentState();
        agent.start();
        while (!move.equals("x"))
        {
            System.out.println(board.toConsole());

            System.out.println("Please enter next move:");
            move = console.readLine();

            System.out.println();
            if (move.contains("x")) {agent.Stop(); return;}
            if (move.contains("p"))
            {
                agent.pause();
                continue;
            }
            if (board.isLegalMove(move))
            {
                Piece taken = board.getPiece(move.split(",")[1]);
                if(taken.type != Type.VACANT) Board.announceCapture(board.getPiece(move.split(",")[0]),taken);
                board = board.move(move);
                board = agent.updateState(board);
            }
            else continue;

            System.out.println(board.toConsole());

            System.out.println("\nruminating...");
            board = agent.act();
            System.out.println();
        }
    }

    private static void play(Board board) throws IOException
    {
        String move = "";
        while (!move.equals("x"))
        {
            System.out.println(board.toConsole());

            System.out.println("Please enter next move:");
            move = console.readLine();

            System.out.println();
            if (board.isLegalMove(move))
                board = board.move(move);

            board.makeRoot();

            System.out.println(board.toConsole());
            System.out.println("\nruminating...\n");
//            board = board.doWhite();
            board = board.doBlack();

            board.makeRoot();

            // todo: board = Agent.act(board);
        }
    }

    private static void explore(Board board, int depth)
    {
        int size = 0;
        LocalDateTime now, then = LocalDateTime.now();
        try {board.minMax(depth);}
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
        System.out.println("press 'enter' to terminate");
        awaitInput();
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

    @SneakyThrows private static String awaitInput(){return console.readLine();}

    private static void font()
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();

        Toolkit toolkit =  Toolkit.getDefaultToolkit();

        for (Font font : allFonts)
        {
            if (!font.getFontName().toLowerCase().contains("mono")) continue;

            FontMetrics fontMetrics = toolkit.getFontMetrics(font.deriveFont(12f));
            System.out.println(font.getFontName());
            System.out.println("♚ = " + fontMetrics.charWidth('♚'));
            for (char j = 0; j < Character.MAX_VALUE; j++)
            {
                if (fontMetrics.charWidth(j) == fontMetrics.charWidth('♚'))
                    System.out.print("\t" + j);
            }
            System.out.println();
//            System.out.println(Arrays.toString(fontMetrics.charWidth()));
        }

        Board board = new Board().move("h2,h3");
        System.out.println(board);
    }

}
