import java.io.*;
import java.util.*;

public class EscampeBoard implements Partie1 {

    private int[][] boardTypes = {
        {1, 2, 2, 3, 1, 2},
        {3, 1, 3, 1, 3, 2},
        {2, 3, 1, 2, 1, 3},
        {2, 1, 3, 2, 3, 1},
        {1, 3, 1, 3, 1, 2},
        {3, 2, 2, 1, 3, 2}
    };

    private Piece[][] board = new Piece[6][6];
    private String currentPlayer = "blanc";
    private Position lastOpponentDest = null;

    @Override
    public void setFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null && row < 6) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;

                String content = line.substring(3, 9); // extrait les 6 caractères du plateau
                for (int col = 0; col < 6; col++) {
                    char symbol = content.charAt(col);
                    switch (symbol) {
                        case 'b': board[row][col] = new Piece("paladin", "noir"); break;
                        case 'B': board[row][col] = new Piece("licorne", "noir"); break;
                        case 'n': board[row][col] = new Piece("paladin", "blanc"); break;
                        case 'N': board[row][col] = new Piece("licorne", "blanc"); break;
                        case '-': board[row][col] = null; break;
                        default: System.err.println("Symbole non reconnu : " + symbol);
                    }
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }


    @Override
    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("% ABCDEF\n");
            for (int row = 0; row < 6; row++) {
                writer.write(String.format("%02d ", row + 1));
                for (int col = 0; col < 6; col++) {
                    Piece p = board[row][col];
                    if (p == null) writer.write("-");
                    else if (p.color.equals("noir"))
                        writer.write(p.type.equals("licorne") ? "B" : "b");
                    else if (p.color.equals("blanc"))
                        writer.write(p.type.equals("licorne") ? "N" : "n");
                }
                writer.write(String.format(" %02d\n", row + 1));
            }
            writer.write("% ABCDEF\n");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }

    @Override
    public boolean isValidMove(String move, String player) {
        if (move.equals("E")) return true;

        if (move.contains("/")) return player.equals("noir") || player.equals("blanc");

        String[] parts = move.split("-");
        if (parts.length != 2) return false;

        Position from = Position.fromString(parts[0]);
        Position to = Position.fromString(parts[1]);

        Piece p = board[from.row][from.col];
        if (p == null || !p.color.equals(player)) return false;

        if (lastOpponentDest != null) {
            int requiredLisere = boardTypes[lastOpponentDest.row][lastOpponentDest.col];
            if (boardTypes[from.row][from.col] != requiredLisere) return false;
        }

        int dx = Math.abs(to.col - from.col);
        int dy = Math.abs(to.row - from.row);
        if (dx != 0 && dy != 0) return false;

        int dist = dx + dy;
        int lisere = boardTypes[from.row][from.col];
        if (dist != lisere) return false;

        int stepRow = Integer.compare(to.row, from.row);
        int stepCol = Integer.compare(to.col, from.col);
        int r = from.row + stepRow;
        int c = from.col + stepCol;

        for (int i = 1; i < dist; i++) {
            if (board[r][c] != null) return false;
            r += stepRow;
            c += stepCol;
        }

        Piece destPiece = board[to.row][to.col];
        if (destPiece != null && destPiece.color.equals(player)) return false;

        return true;
    }

    @Override
    public String[] possiblesMoves(String player) {
        List<String> moves = new ArrayList<>();
        if (lastOpponentDest == null) return new String[] {}; // Aucun coup imposé

        int lisere = boardTypes[lastOpponentDest.row][lastOpponentDest.col];

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                Piece p = board[r][c];
                if (p != null && p.color.equals(player) && boardTypes[r][c] == lisere) {
                    Position from = new Position(r, c);
                    for (int[] dir : new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}}) {
                        int nr = r + dir[0]*lisere;
                        int nc = c + dir[1]*lisere;
                        if (nr >= 0 && nr < 6 && nc >= 0 && nc < 6) {
                            Position to = new Position(nr, nc);
                            String move = from.toString() + "-" + to.toString();
                            if (isValidMove(move, player)) {
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }

        return moves.toArray(new String[0]);
    }

    @Override
    public void play(String move, String player) {
        if (move.equals("E")) {
            currentPlayer = player.equals("blanc") ? "noir" : "blanc";
            lastOpponentDest = null;
            return;
        }

        if (move.contains("/")) {
            String[] positions = move.split("/");
            for (int i = 0; i < positions.length; i++) {
                Position pos = Position.fromString(positions[i]);
                if (i == 0)
                    board[pos.row][pos.col] = new Piece("licorne", player);
                else
                    board[pos.row][pos.col] = new Piece("paladin", player);
            }
        } else {
            String[] parts = move.split("-");
            Position from = Position.fromString(parts[0]);
            Position to = Position.fromString(parts[1]);

            Piece p = board[from.row][from.col];
            board[from.row][from.col] = null;
            board[to.row][to.col] = p;

            lastOpponentDest = to;
        }

        currentPlayer = player.equals("blanc") ? "noir" : "blanc";
    }

    @Override
    public boolean gameOver() {
        boolean licorneNoir = false, licorneBlanc = false;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                Piece p = board[r][c];
                if (p != null && p.type.equals("licorne")) {
                    if (p.color.equals("noir")) licorneNoir = true;
                    if (p.color.equals("blanc")) licorneBlanc = true;
                }
            }
        }
        return !(licorneNoir && licorneBlanc);
    }
    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();

        // Exemple de placement initial (Blanc : C1 licorne, A3 C2 C5 F1 F4 paladins / Noir : C6 licorne, A6 B5 D5 E6 F5 paladins)
        board.play("C6/A6/B5/D5/E6/F5", "noir");
        board.play("C1/A3/C2/C5/F1/F4", "blanc");

        // Sauvegarde du plateau dans un fichier
        String saveFile = "plateau_test.txt";
        board.saveToFile(saveFile);
        System.out.println("Plateau sauvegardé dans " + saveFile);

        // Création d'un nouveau plateau pour tester le chargement
        EscampeBoard boardLoaded = new EscampeBoard();
        boardLoaded.setFromFile(saveFile);
        System.out.println("Plateau chargé depuis " + saveFile);

        // Re-sauvegarde dans un autre fichier pour comparer
        String saveFile2 = "plateau_test_copy.txt";
        boardLoaded.saveToFile(saveFile2);
        System.out.println("Plateau recopié dans " + saveFile2);
    }

}
