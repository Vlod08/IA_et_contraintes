package escampe;

import java.util.Random;

/**
 * JoueurIA: IA basique pour le jeu Escampe.
 * Gère la phase d'ouverture (placement) puis joue aléatoirement.
 */
public class JoueurIA implements IJoueur {
    private int numJoueur;        // IJoueur.BLANC or IJoueur.NOIR
    private EscampeBoard board;
    private Random rand;
    private boolean ouvertureNoir;
    private boolean ouvertureBlanc;

    public JoueurIA() {
        this.rand = new Random();
    }

    @Override
    public void initJoueur(int numJoueur) {
        this.numJoueur      = numJoueur;
        this.board          = new EscampeBoard();
        this.ouvertureNoir  = true;
        this.ouvertureBlanc = true;
    }

    @Override
    public int getNumJoueur() {
        return this.numJoueur;
    }

    @Override
    public String choixMouvement() {
        String joueurStr = (numJoueur == IJoueur.BLANC) ? "blanc" : "noir";

        // --- Phase d'ouverture ---
        if (numJoueur == IJoueur.NOIR && ouvertureNoir) {
            String placement = "A1/B1/C1/D1/E1/F1";
            ouvertureNoir = false;
            System.out.println("Ouverture Noir : placement " + placement);
            board.play(placement, joueurStr);
            return placement;
        }
        if (numJoueur == IJoueur.BLANC && ouvertureBlanc) {
            String placement = "A6/B6/C6/D6/E6/F6";
            ouvertureBlanc = false;
            System.out.println("Ouverture Blanc : placement " + placement);
            board.play(placement, joueurStr);
            return placement;
        }

        // --- Jeu normal ---
        System.out.println("Ah, c'est à moi, le joueur IA " + joueurStr.toUpperCase() + " de jouer... Je réfléchis...");
        System.out.println("Voici mon plateau de jeu avant de choisir mon coup :");
        afficherPlateau();

        String[] coups = board.possiblesMoves(joueurStr);

        if (coups == null || coups.length == 0) {
            System.out.println("Aucun coup disponible.");
            board.play("E", joueurStr);
            return "E";
        }
        if (coups.length == 1 && "E".equals(coups[0])) {
            System.out.println("Aucun coup possible. Je passe mon tour.");
            board.play("E", joueurStr);
            return "E";
        }

        // Affichage des coups possibles
        System.out.println(coups.length + " coups possibles :");
        for (int i = 0; i < coups.length; i++) {
            System.out.print(coups[i]);
            if (i < coups.length - 1) System.out.print(" | ");
        }
        System.out.println();

        // Choix aléatoire (à remplacer par Minimax)
        String move = coups[rand.nextInt(coups.length)];
        System.out.println("Je choisis de jouer " + move);
        board.play(move, joueurStr);

        System.out.println("Voici mon plateau de jeu après mon coup :");
        afficherPlateau();

        return move;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        String adv = (numJoueur == IJoueur.BLANC) ? "noir" : "blanc";
        if ("PASSE".equals(coup) || "E".equals(coup)) {
            board.play("E", adv);
        } else {
            board.play(coup, adv);
        }
    }

    @Override
    public void declareLeVainqueur(int vainqueur) {
        String gagnant = (vainqueur == IJoueur.BLANC) ? "BLANC" : "NOIR";
        System.out.println("Le vainqueur est : " + gagnant);
    }

    @Override
    public String binoName() {
        return "IA-Escampe";
    }

    /** Affiche le plateau via System.out */
    private void afficherPlateau() {
        System.out.println("   ABCDEF");
        Piece[][] mat = board.getBoard();
        for (int r = 0; r < 6; r++) {
            System.out.printf("%02d ", r + 1);
            for (int c = 0; c < 6; c++) {
                Piece p = mat[r][c];
                char ch = '-';
                if (p != null) {
                    if ("noir".equals(p.color)) {
                        ch = p.type.equals("licorne") ? 'N' : 'n';
                    } else {
                        ch = p.type.equals("licorne") ? 'B' : 'b';
                    }
                }
                System.out.print(ch);
            }
            System.out.printf(" %02d%n", r + 1);
        }
        System.out.println("   ABCDEF");
    }
}
