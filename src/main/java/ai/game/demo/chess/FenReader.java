package ai.game.demo.chess;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FenReader {

    public String[] read(String FEN) {
        FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String[] result = new String[8];
        String[] rowSplit = FEN.split("/");
        String[] meta = rowSplit[7].split(" ");
        rowSplit[7] = meta[0];
        for (int i = 0; i < rowSplit.length; i++) {
            result[i] = buildRow(rowSplit[i]);
        }

        result[7] = buildMeta(meta);

        for (String rawr : result) {
            System.out.println(rawr);
        }

        return result;
    }

    private String buildRow(String s) {
        StringBuilder result = new StringBuilder();
        char[] array = s.toCharArray();
        for (char current : array) {
            if (current >= 48 && current <= 56) {
                result.append("ㅤ".repeat(current - 48));
            } else result.append(convertToSymbol(current));
        }
        return result.toString();
    }

    private char convertToSymbol(char c) {
        return switch (c) {
            case 'r' -> '♖';
            case 'n' -> '♘';
            case 'b' -> '♗';
            case 'q' -> '♕';
            case 'k' -> '♔';
            case 'p' -> '♙';
            case 'R' -> '♜';
            case 'N' -> '♞';
            case 'B' -> '♝';
            case 'Q' -> '♛';
            case 'K' -> '♚';
            case 'P' -> '♟';
            default -> throw new IllegalStateException("Unexpected value: " + c);
        };
    }

    public String buildMeta(String[] meta) {
        StringBuilder result = new StringBuilder();
        String[] contents = new String[]{"a1a1", "w", "p", "passantTarget", "W", "W", "W", "B", "B", "B"};
        contents[1] = meta[1]; //turn
        contents[3] = !meta[3].equals("-") ? meta[3] : "a1"; //passantTarget

        String[] finalized = transCastling(contents, meta[2]); // translating castling logic

        for (String crt : finalized) {
            result.append(crt);
        }
        return result.toString();
    }

    private String[] transCastling(String[] meta, String input) {
        String[] result = new String[]{"c", "c", "c", "c", "c", "c"};

        switch (input) {
            case "KQkq" -> result = new String[]{"c", "c", "c", "c", "c", "c"};
            case "KQk" -> result = new String[]{"c", "c", "c", "c", "c", " "};
            case "KQq" -> result = new String[]{"c", "c", "c", "c", " ", "c"};
            case "Qqk" -> result = new String[]{"c", " ", "c", "c", "c", "c"};
            case "Kqk" -> result = new String[]{"c", "c", " ", "c", "c", "c"};
            case "KQ" -> result = new String[]{"c", "c", "c", " ", " ", " "};
            case "Kq" -> result = new String[]{"c", "c", " ", "c", " ", "c"};
            case "Kk" -> result = new String[]{"c", "c", " ", "c", "c", " "};
            case "Qk" -> result = new String[]{"c", " ", "c", "c", "c", " "};
            case "Qq" -> result = new String[]{"c", " ", "c", "c", " ", "c"};
            case "kq" -> result = new String[]{" ", " ", " ", "c", "c", "c"};
            case "K" -> result = new String[]{"c", " ", "c", " ", " ", " "};
            case "Q" -> result = new String[]{"c", "c", " ", " ", " ", " "};
            case "k" -> result = new String[]{" ", " ", " ", "c", " ", "c"};
            case "q" -> result = new String[]{" ", " ", " ", "c", "c", " "};
            default ->  throw new IllegalStateException("Unexpected value: " + input);
            // todo: might need mirroring depending on formatting
        }

        // translating castling logic
        System.arraycopy(result, 0, meta, 4, result.length);
        return meta;
    }

    public String write(String[] ChessArray) {
        String result = "fuwah";

        return result;
    }
}
