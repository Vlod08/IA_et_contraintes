package escampe;

import java.util.Random;

public class JoueurIA implements IJoueur {

    private int numJoueur; // 0 = blanc, 1 = noir
    private EscampeBoard board;
    private Random rand;

    public JoueurIA() {
        this.rand = new Random();
    }

    @Override
    public void initJoueur(int numJoueur) {
        this.numJoueur = numJoueur;
        this.board = new EscampeBoard();
    }

    @Override
    public String choixMouvement() {
        String joueur = (numJoueur == 0) ? "blanc" : "noir";

        System.out.println("Ah, c'est à moi, le joueur IA " + joueur.toUpperCase() + " de jouer... Je réfléchis...");
        System.out.println("Voici mon plateau de jeu avant de choisir mon coup :");
        afficherPlateau();

        String[] coups = board.possiblesMoves(joueur);

        if (coups == null || coups.length == 0) {
            System.out.println("Aucun coup disponible.");
            return "xxxxx"; // ou autre convention de signalement d'erreur
        }

        if (coups.length == 1 && coups[0].equals("E")) {
            System.out.println("Aucun coup possible. Je passe mon tour.");
            return "E";
        }

        System.out.println(coups.length + " Coups :");
        for (int i = 0; i < coups.length; i++) {
            System.out.print(coups[i]);
            if (i < coups.length - 1) {
                System.out.print(" | ");
            }
        }
        System.out.println();

        int idx = rand.nextInt(coups.length);
        String move = coups[idx];

        System.out.println("Je choisi de jouer " + move);
        board.play(move, joueur);

        System.out.println("Voici mon plateau de jeu après mon coup :");
        afficherPlateau();

        return move;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        String joueurAdverse = (numJoueur == 0) ? "noir" : "blanc";
        board.play(coup, joueurAdverse);
    }

    @Override
    public void declareLeVainqueur(int vainqueur) {
        String gagnant = (vainqueur == 0) ? "BLANC" : "NOIR";
        System.out.println("Le vainqueur est : " + gagnant);
    }

    @Override
    public String binoName() {
        return "IA-Escampe";
    }

    @Override
    public int getNumJoueur() {
        return this.numJoueur;
    }

    private void afficherPlateau() {
        System.out.println("   ABCDEF");
        for (int row = 0; row < 6; row++) {
            System.out.printf("%02d ", row + 1);
            for (int col = 0; col < 6; col++) {
                Piece p = board.getBoard()[row][col];
                char symbol = '-';
                if (p != null) {
                    if (p.color.equals("noir")) {
                        symbol = p.type.equals("licorne") ? 'N' : 'n';
                    } else {
                        symbol = p.type.equals("licorne") ? 'B' : 'b';
                    }
                }
                System.out.print(symbol);
            }
            System.out.printf(" %02d\n", row + 1);
        }
        System.out.println("   ABCDEF");
    }
}
