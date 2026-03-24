
import chess.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class _main
{
    public static void main(String[] args) throws IOException
    {
//        for (char c:Type.black.toCharArray()) {System.out.println(c + ": " + ((int)c));}
//        for (char c:Type.white.toCharArray()) {System.out.println(c + ": " + ((int)c));}

        Board board = new Board(new String[]
            {
                    "♖♘♗♕♔♗♘♖",
                    "♙♙♙♙♙♙♙♙",
                    "        ",
                    "        ",
                    "        ",
                    "        ",
                    "♟♟♟♟♟♟♟♟",
                    "♜♞♝♛♚♝♞♜",
            });

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        String move = "";

        while (!move.equals("x"))
        {
            System.out.println(board);

            System.out.println("Please enter next move:");
            move = r.readLine();

            System.out.println();
            if (board.isLegalMove(move)) board = board.move(move).invert();
        }

        System.out.println("Goodbye");
    }
}
