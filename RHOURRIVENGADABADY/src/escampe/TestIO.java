package escampe;

import java.nio.file.*;
import java.util.*;

public class TestIO {
  public static void main(String[] args) throws Exception {
    String in  = args.length > 0 ? args[0] : "src/escampe/plateau_input.txt";
    String out = args.length > 1 ? args[1] : "src/escampe/plateau_output.txt";
    EscampeBoard board = new EscampeBoard();
    board.setFromFile(in);
    board.saveToFile(out);
    
    List<String> lin = Files.readAllLines(Paths.get(in));
    List<String> lout = Files.readAllLines(Paths.get(out));
    // Keep only the 6 board lines (strip comments & blank)
    lin.removeIf(l -> l.trim().isEmpty() || l.startsWith("%"));
    lout.removeIf(l -> l.trim().isEmpty() || l.startsWith("%"));
    
    if (lin.equals(lout)) {
      System.out.println("SUCCESS: IO matches!");
    } else {
      System.out.println("FAIL: IO mismatch");
      for (int i = 0; i < Math.min(lin.size(), lout.size()); i++) {
        if (!lin.get(i).equals(lout.get(i))) {
          System.out.printf("Line %d: expected '%s' but got '%s'%n", i+1, lin.get(i), lout.get(i));
        }
      }
    }
  }
}

