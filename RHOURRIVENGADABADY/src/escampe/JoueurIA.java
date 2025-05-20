package escampe;

import java.io.File;
import java.util.Random;

/**
 * IA Escampe avec fonction minimax séparée.
 */
public class JoueurIA implements IJoueur {
    private int myColour;              // IJoueur.BLANC or IJoueur.NOIR
    private EscampeBoard board;
    private boolean ouvertureNoir;
    private boolean ouvertureBlanc;
    private Random rand = new Random();

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
            System.out.println("Ouverture Noir : " + move);
            board.play(move, me);
            return move;
        }
        if (myColour == IJoueur.BLANC && ouvertureBlanc) {
            ouvertureBlanc = false;
            String move = "C1/A3/C2/C5/F1/F4";
            System.out.println("Ouverture Blanc : " + move);
            board.play(move, me);
            return move;
        }

        // --- Recherche minimax ---
        String best = minimax();
        System.out.println("IA choisit (minimax) : " + best);
        board.play(best, me);
        return best;
    }

    /**
     * Deux-ply minimax : pour chaque coup possible, simule
     * la réponse adverse, évalue avec evaluateBoard, et choisit
     * le coup maximisant le pire scénario.
     */
    private String minimax() {
        String me  = (myColour == IJoueur.NOIR) ? "noir" : "blanc";
        String opp = (myColour == IJoueur.NOIR) ? "blanc" : "noir";

        String[] myMoves = board.possiblesMoves(me);
        // si unique passe
        if (myMoves.length == 1 && "E".equals(myMoves[0])) {
            return "E";
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        String bestMove  = myMoves[rand.nextInt(myMoves.length)];

        try {
            // snapshot de l'état
            File tmp = File.createTempFile("escampe_", ".tmp");
            board.saveToFile(tmp.getAbsolutePath());

            for (String m : myMoves) {
                board.play(m, me);
                String[] replies = board.possiblesMoves(opp);

                double worst = Double.POSITIVE_INFINITY;
                if (replies.length == 1 && "E".equals(replies[0])) {
                    // adversaire seul passe
                    worst = evaluateBoard(me, opp);
                } else {
                    for (String r : replies) {
                        board.play(r, opp);
                        double sc = evaluateBoard(me, opp);
                        board.setFromFile(tmp.getAbsolutePath());
                        worst = Math.min(worst, sc);
                    }
                }

                if (worst > bestScore) {
                    bestScore = worst;
                    bestMove  = m;
                }
                // restaurer pour le coup suivant
                board.setFromFile(tmp.getAbsolutePath());
            }
            tmp.delete();
        } catch (Exception e) {
            // en cas d'erreur IO, retomber sur random
            bestMove = myMoves[rand.nextInt(myMoves.length)];
        }

        return bestMove;
    }

    /**
     * Heuristique simple : mobilité + léger bonus mat�riel.
     */
    private double evaluateBoard(String me, String opp) {
        double mobilityScore = board.possiblesMoves(me).length
                             - board.possiblesMoves(opp).length;
        // compter les pièces restantes
        int myCount = 0, oppCount = 0;
        for (Piece[] row : board.getBoard()) {
            for (Piece p : row) {
                if (p != null) {
                    if (p.color.equals(me))    myCount++;
                    else                        oppCount++;
                }
            }
        }
        double materialScore = 0.1 * (myCount - oppCount);
        return mobilityScore + materialScore;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        String adv = (myColour == IJoueur.NOIR) ? "blanc" : "noir";
        if ("E".equals(coup) || "PASSE".equals(coup)) {
            board.play("E", adv);
        } else {
            board.play(coup, adv);
        }
    }

    @Override
    public void declareLeVainqueur(int winner) {
        System.out.println("=== Vainqueur : " +
            (winner == IJoueur.NOIR ? "NOIR" : "BLANC") + " ===");
    }

    @Override
    public String binoName() {
        return "IA-Escampe-MinMax";
    }
}
