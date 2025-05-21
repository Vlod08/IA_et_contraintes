package escampe;

import java.io.File;
import java.util.Random;

/**
 * IA Escampe avec Minimax récursif et élagage alpha-beta,
 * sans modifier les autres fichiers. Utilise un test terminal
 * interne plutôt que board.isGameOver().
 */
public class JoueurIA implements IJoueur {
    private int myColour;              // IJoueur.BLANC or IJoueur.NOIR
    private EscampeBoard board;
    private boolean ouvertureNoir;
    private boolean ouvertureBlanc;
    private final Random rand = new Random();
    private static final int MAX_DEPTH = 4;  // Profondeur ajustable

    @Override
    public void initJoueur(int mycolour) {
        this.myColour       = mycolour;
        this.board          = new EscampeBoard();
        this.ouvertureNoir  = true;
        this.ouvertureBlanc = true;
    }

    @Override
    public int getNumJoueur() {
        return myColour;
    }

    @Override
    public String choixMouvement() {
        String me  = (myColour == IJoueur.NOIR) ? "noir" : "blanc";

        // --- Phase d'ouverture ---
        if (myColour == IJoueur.NOIR && ouvertureNoir) {
            ouvertureNoir = false;
            String move = "C6/A6/B5/D5/E6/F5";
            board.play(move, me);
            return move;
        }
        if (myColour == IJoueur.BLANC && ouvertureBlanc) {
            ouvertureBlanc = false;
            String move = "C1/A3/C2/C5/F1/F4";
            board.play(move, me);
            return move;
        }

        // --- Recherche Minimax récursif avec alpha-beta ---
        String bestMove = minimax(MAX_DEPTH);
        board.play(bestMove, me);
        return bestMove;
    }

    /**
     * Pilote Minimax : teste chaque coup racine et renvoie le meilleur.
     */
    private String minimax(int depth) {
        String me  = (myColour == IJoueur.NOIR) ? "noir" : "blanc";
        String opp = (myColour == IJoueur.NOIR) ? "blanc" : "noir";

        String[] myMoves = board.possiblesMoves(me);
        if (myMoves.length == 1 && "E".equals(myMoves[0])) {
            return "E";
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        String bestMove = myMoves[rand.nextInt(myMoves.length)];

        try {
            File snapshot = File.createTempFile("escampe_init_", ".tmp");
            board.saveToFile(snapshot.getAbsolutePath());

            for (String move : myMoves) {
                board.play(move, me);
                double score = minimaxValue(depth - 1, false,
                                            Double.NEGATIVE_INFINITY,
                                            Double.POSITIVE_INFINITY,
                                            me, opp);
                board.setFromFile(snapshot.getAbsolutePath());

                if (score > bestScore) {
                    bestScore = score;
                    bestMove  = move;
                }
            }
            snapshot.delete();
        } catch (Exception e) {
            bestMove = myMoves[rand.nextInt(myMoves.length)];
        }

        return bestMove;
    }

    /**
     * Minimax récursif avec élagage alpha-beta.
     */
    private double minimaxValue(int depth,
                                boolean isMaximizing,
                                double alpha,
                                double beta,
                                String player,
                                String opponent) throws Exception {
        // Test terminal interne
        if (depth == 0 || isTerminal(player, opponent)) {
            return evaluateBoard(player, opponent);
        }

        String[] moves = board.possiblesMoves(isMaximizing ? player : opponent);
        if (moves.length == 0) {
            return evaluateBoard(player, opponent);
        }

        File snapshot = File.createTempFile("escampe_", ".tmp");
        board.saveToFile(snapshot.getAbsolutePath());

        double best = isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (String move : moves) {
            board.play(move, isMaximizing ? player : opponent);
            double val = minimaxValue(depth - 1,
                                      !isMaximizing,
                                      alpha, beta,
                                      player, opponent);
            board.setFromFile(snapshot.getAbsolutePath());

            if (isMaximizing) {
                best = Math.max(best, val);
                alpha = Math.max(alpha, val);
            } else {
                best = Math.min(best, val);
                beta  = Math.min(beta, val);
            }
            if (beta <= alpha) break;
        }
        snapshot.delete();
        return best;
    }

    /**
     * Test terminal: double passe ou plus de pièces pour un joueur.
     */
    private boolean isTerminal(String player, String opponent) {
        String[] pMoves = board.possiblesMoves(player);
        String[] oMoves = board.possiblesMoves(opponent);
        // double passe
        if (pMoves.length == 1 && "E".equals(pMoves[0])
         && oMoves.length == 1 && "E".equals(oMoves[0])) {
            return true;
        }
        // plus de pièces
        int countP = 0, countO = 0;
        for (Piece[] row : board.getBoard()) {
            for (Piece pc : row) {
                if (pc != null) {
                    if (pc.color.equals(player)) countP++;
                    else                           countO++;
                }
            }
        }
        return (countP == 0 || countO == 0);
    }

    /**
     * Heuristique : mobilité + petit bonus matériel.
     */
    private double evaluateBoard(String me, String opp) {
        double mobility = board.possiblesMoves(me).length
                        - board.possiblesMoves(opp).length;
        int myCount = 0, oppCount = 0;
        for (Piece[] row : board.getBoard()) {
            for (Piece p : row) {
                if (p != null) {
                    if (p.color.equals(me)) myCount++;
                    else                      oppCount++;
                }
            }
        }
        double material = 0.1 * (myCount - oppCount);
        return mobility + material;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        String adv = (myColour == IJoueur.NOIR) ? "blanc" : "noir";
        board.play("E".equals(coup) || "PASSE".equals(coup) ? "E" : coup, adv);
    }

    @Override
    public void declareLeVainqueur(int winner) {
        // Optional: log the winner
    }

    @Override
    public String binoName() {
        return "IA-Escampe-Minimax-AB";
    }
}
