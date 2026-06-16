package ai.game.demo;

import ai.game.demo.chess.*;
import ai.game.demo.util.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;


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
        System.out.println("usable space: "+(store.getUsableSpace()>>30)+" GB");

        FenReader reader = new FenReader();
        Board board = new Board(reader.read("rnbqkbnr/pp1ppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1"));
        play(board);



        System.out.println("goodbye");
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
            if (board.isLegalMove(move)!=null) board= board.move(move);
            else
            {
                System.out.print  ("\n\033[33;3m Illegal move:"+move+"\033[0m");
                System.out.println("\n\033[33;3m Please enter \"from , to\" as eg. \"a1,b2\" \033[0m");
            }
        }
    }
}
