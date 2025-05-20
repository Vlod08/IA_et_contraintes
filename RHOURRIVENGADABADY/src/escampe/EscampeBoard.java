
package escampe;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EscampeBoard implements Partie1 {

    private static final int SIZE = 6;
    private static final int[][] boardTypes = {
        {1, 2, 2, 3, 1, 2},
        {3, 1, 3, 1, 3, 2},
        {2, 3, 1, 2, 1, 3},
        {2, 1, 3, 2, 3, 1},
        {1, 3, 1, 3, 1, 2},
        {3, 2, 2, 1, 3, 2}
    };

    private Piece[][] board = new Piece[SIZE][SIZE];
    private String currentPlayer = "blanc";
    private Position lastOpponentDest = null;  // null until first normal move

    @Override
    public void setFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            List<String> boardLines = new ArrayList<>();
            String line;
            // 1) collect exactly 6 non-comment, non-blank lines
            while (boardLines.size() < SIZE && (line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                boardLines.add(line);
            }
            if (boardLines.size() != SIZE) {
                throw new RuntimeException("Fichier invalide : attendu 6 lignes, trouvé " + boardLines.size());
            }

            // 2) parse each line with a regex: two digits, optional sep, six chars, optional sep, two digits
            Pattern p = Pattern.compile("^\\d{1,2}\\s*([NnBb\\-]{6})\\s*\\d{1,2}$");
            for (int i = 0; i < SIZE; i++) {
                String rowLine = boardLines.get(i);
                Matcher m = p.matcher(rowLine);
                if (!m.matches()) {
                    throw new RuntimeException("Ligne invalide : " + rowLine);
                }
                String content = m.group(1);  // the six-board-character substring
                for (int j = 0; j < SIZE; j++) {
                    char c = content.charAt(j);
                    switch (c) {
                        case 'N': board[i][j] = new Piece("licorne", "noir");  break;
                        case 'n': board[i][j] = new Piece("paladin", "noir"); break;
                        case 'B': board[i][j] = new Piece("licorne", "blanc"); break;
                        case 'b': board[i][j] = new Piece("paladin", "blanc"); break;
                        case '-': board[i][j] = null;                          break;
                        default:  throw new RuntimeException("Symbole non reconnu : " + c);
                    }
                }
            }

            // (re)initialize turn / lastLisere if your code needs it…
            this.lastOpponentDest = null;
            this.currentPlayer   = "blanc";

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier", e);
        }
    }

    @Override
    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("% ABCDEF\n");
            for (int row = 0; row < SIZE; row++) {
                writer.write(String.format("%02d ", row + 1));
                for (int col = 0; col < SIZE; col++) {
                    Piece p = board[row][col];
                    char symbol = '-';
                    if (p != null) {
                        // Uppercase = licorne; lowercase = paladin
                        if (p.color.equals("noir")) {
                            symbol = p.type.equals("licorne") ? 'N' : 'n';
                        } else {
                            symbol = p.type.equals("licorne") ? 'B' : 'b';
                        }
                    }
                    writer.write(symbol);
                }
                writer.write(String.format(" %02d\n", row + 1));
            }
            writer.write("% ABCDEF\n");
        } catch (IOException e) {
            throw new RuntimeException("Erreur écriture fichier", e);
        }
    }

    @Override
    public boolean isValidMove(String move, String player) {
        // 1) Pass only if no other move is available
        if ("E".equals(move)) {
            String[] pm = possiblesMoves(player);
            return pm.length == 1 && "E".equals(pm[0]);
        }

        // 2) Opening placement: exactly 6 positions, all empty, and it's your turn
        if (move.contains("/")) {
            if (!player.equals(currentPlayer)) return false;
            String[] cells = move.split("/");
            if (cells.length != SIZE) return false;
            for (String s : cells) {
                Position pos = Position.fromString(s);
                if (board[pos.row][pos.col] != null) return false;
            }
            return true;
        }

        // 3) Normal move must be "C1-D4"
        String[] parts = move.split("-");
        if (parts.length != 2) return false;
        Position from = Position.fromString(parts[0]);
        Position to   = Position.fromString(parts[1]);

        Piece p = board[from.row][from.col];
        if (p == null || !p.color.equals(player)) return false;

        // 4) Lisere constraint (except first move)
        if (lastOpponentDest != null) {
            int required = boardTypes[lastOpponentDest.row][lastOpponentDest.col];
            if (boardTypes[from.row][from.col] != required) return false;
        }

        // 5) Orthogonal, exact distance
        int dr = to.row - from.row, dc = to.col - from.col;
        if (dr != 0 && dc != 0) return false;
        int dist = Math.abs(dr) + Math.abs(dc);
        int allowed = boardTypes[from.row][from.col];
        if (dist != allowed) return false;

        // 6) Path must be clear (except capture square)
        int stepR = Integer.signum(dr), stepC = Integer.signum(dc);
        int r = from.row, c = from.col;
        for (int i = 1; i <= dist; i++) {
            r += stepR; c += stepC;
            if (i < dist && board[r][c] != null) return false;
        }

        // 7) Capture rules: only paladin can capture enemy licorne
        Piece dest = board[to.row][to.col];
        if (dest != null) {
            if (!p.type.equals("paladin") ||
                !dest.type.equals("licorne") ||
                dest.color.equals(p.color)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String[] possiblesMoves(String player) {
        List<String> moves = new ArrayList<>();

        // Generate real moves
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = board[r][c];
                if (p == null || !p.color.equals(player)) continue;
                if (lastOpponentDest != null) {
                    int req = boardTypes[lastOpponentDest.row][lastOpponentDest.col];
                    if (boardTypes[r][c] != req) continue;
                }
                int d = boardTypes[r][c];
                for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                    int nr = r + dir[0]*d, nc = c + dir[1]*d;
                    if (nr<0||nr>=SIZE||nc<0||nc>=SIZE) continue;
                    String m = Position.toString(r,c) + "-" + Position.toString(nr,nc);
                    if (isValidMove(m, player)) moves.add(m);
                }
            }
        }

        // If none, the only legal move is "E"
        if (moves.isEmpty()) {
            return new String[]{ "E" };
        }
        return moves.toArray(new String[0]);
    }

    @Override
    public void play(String move, String player) {
        if (!isValidMove(move, player))
            throw new IllegalArgumentException("Coup invalide : " + move);

        // 1) Pass
        if ("E".equals(move)) {
            lastOpponentDest = null;
            currentPlayer = opponentOf(player);
            return;
        }

        // 2) Opening placement
        if (move.contains("/")) {
            String[] cells = move.split("/");
            for (int i = 0; i < cells.length; i++) {
                Position pos = Position.fromString(cells[i]);
                if (i == 0) board[pos.row][pos.col] = new Piece("licorne", player);
                else      board[pos.row][pos.col] = new Piece("paladin",  player);
            }
            // no lisere constraint yet
            lastOpponentDest = null;
            currentPlayer = opponentOf(player);
            return;
        }

        // 3) Normal move
        String[] parts = move.split("-");
        Position from = Position.fromString(parts[0]);
        Position to   = Position.fromString(parts[1]);
        Piece p = board[from.row][from.col];
        board[from.row][from.col] = null;
        board[to.row][to.col] = p;  // overwrite or capture

        lastOpponentDest = to;
        currentPlayer = opponentOf(player);
    }

    @Override
    public boolean gameOver() {
        boolean hasNoir = false, hasBlanc = false;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = board[r][c];
                if (p != null && p.type.equals("licorne")) {
                    if (p.color.equals("noir"))  hasNoir = true;
                    if (p.color.equals("blanc")) hasBlanc= true;
                }
            }
        }
        return !(hasNoir && hasBlanc);
    }

    private String opponentOf(String p) {
        return p.equals("blanc") ? "noir" : "blanc";
    }
    public Piece[][] getBoard() {
        return board;
    }
}
