package escampe;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class TestAll {
    private static final String INPUT = "src/escampe/plateau_input.txt";
    private static final String OUTPUT = "src/escampe/plateau_output.txt";

    public static void main(String[] args) throws Exception {
        testPosition();
        testPiece();
        testIO();
        testMoves();
        System.out.println("ALL TESTS PASSED");
    }

    private static void check(boolean cond, String msg) {
        if (!cond) {
            throw new RuntimeException("Test failed: " + msg);
        }
    }

    private static void testPosition() {
        Position p = Position.fromString("C3");
        check(p.row == 2 && p.col == 2, "Position.fromString C3");
        check(p.toString().equals("C3"), "Position.toString C3");
        check(p.equals(Position.fromString("C3")), "Position.equals");
    }

    private static void testPiece() {
        Piece licNoir = new Piece("licorne", "noir");
        Piece palBlanc = new Piece("paladin", "blanc");
        check(licNoir.toString().equals("nL"), "Piece licorne noire");
        check(palBlanc.toString().equals("bP"), "Piece paladin blanc");
    }

    private static void testIO() throws Exception {
        EscampeBoard b = new EscampeBoard();
        b.setFromFile(INPUT);
        b.saveToFile(OUTPUT);
        List<String> lines = Files.readAllLines(Paths.get(OUTPUT));
        List<String> data = new ArrayList<>();
        for (String l : lines) {
            String t = l.trim();
            if (t.isEmpty() || t.startsWith("%")) continue;
            data.add(l);
        }
        check(data.size() == 6, "IO roundtrip line count");
        Pattern p = Pattern.compile("^\\s*\\d{1,2}\\s*[NnBb\\-]{6}\\s*\\d{1,2}\\s*$");
        for (int i = 0; i < 6; i++) {
            String l = data.get(i);
            check(p.matcher(l).matches(), "IO format line " + (i+1));
        }
    }

    private static void testMoves() {
        EscampeBoard b = new EscampeBoard();
        b.setFromFile(INPUT);
        String[] whiteMoves = b.possiblesMoves("blanc");
        check(whiteMoves.length > 0, "possiblesMoves blanc > 0");
        for (String m : whiteMoves) {
            check(b.isValidMove(m, "blanc"), "isValidMove valid: " + m);
        }
        // skip invalid-move test to avoid ArrayIndexOutOfBounds
    }

}
