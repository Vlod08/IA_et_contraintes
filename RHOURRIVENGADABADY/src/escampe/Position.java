
package escampe;

public class Position {
    public int row; // 0 à 5
    public int col; // 0 à 5

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public static Position fromString(String s) {
        int col = s.charAt(0) - 'A';
        int row = Integer.parseInt(s.substring(1)) - 1;
        return new Position(row, col);
    }

    public String toString() {
        return "" + (char)(col + 'A') + (row + 1);
    }

    public static String toString(int row, int col) {
        return new Position(row, col).toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }
}
