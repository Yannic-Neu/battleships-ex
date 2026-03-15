package battleships_ex.gdx.model.board;

/**
 * Represents a single tile on the game board.
 */
public class Cell {

    private final Coordinate position;
    private boolean hasShip;
    private boolean isHit;

    public Cell(Coordinate position) {
        this.position = position;
        this.hasShip = false;
        this.isHit = false;
    }

    public Coordinate getPosition() {
        return position;
    }

    public boolean hasShip() {
        return hasShip;
    }

    public void setHasShip(boolean hasShip) {
        this.hasShip = hasShip;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        this.isHit = hit;
    }
}
