package battleships_ex.gdx.model.board;

import java.util.Objects;

public class Coordinate {
    private final int row;
    private final int col;
    public Coordinate(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Row and Column must be non-negative.");
        }
        this.row = row;
        this.col = col;
    }
    public int getRow() {
        return this.row;
    }
    public int getCol() {
        return this.col;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return row == that.row && col == that.col;
    }
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    @Override
    public String toString() {
        return "Coordinate{" +
            "row = " + row +
            ", col = " + col +
            "}";
    }
}
