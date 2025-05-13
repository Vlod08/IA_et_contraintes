public class Piece {
    public String type;   // "licorne" ou "paladin"
    public String color;  // "noir" ou "blanc"

    public Piece(String type, String color) {
        this.type = type;
        this.color = color;
    }

    public String toString() {
        return color.charAt(0) + (type.equals("licorne") ? "L" : "P");
    }
}
