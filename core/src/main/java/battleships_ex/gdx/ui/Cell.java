package battleships_ex.gdx.ui;

public class Cell {
    private final Coordinate coordinate;
    private Ship ship;
    private boolean hit;
    public Cell(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate must not be null.");
        }
        this.coordinate = coordinate;
        this.ship = null;
        this.hit = false;
    }
    public Coordinate getCoordinate() {
        return coordinate;
    }
    public Ship getShip() {
        return ship;
    }
    public boolean hasShip() {
        return ship != null;
    }
    public boolean isEmpty() {
        return ship == null;
    }
    public void placeShip(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null.");
        }
        if (this.ship != null) {
            throw new IllegalStateException("Cell already contains a ship.");
        }
        this.ship = ship;
    }
    public boolean isHit() {
        return hit;
    }
    public void markHit() {
        this.hit = true;
    }
    public boolean hasBeenHitAndContainsShip() {
        return hit && ship != null;
    }
    @Override
    public String toString() {
        return "Cell{" +
            "coordinate = " + coordinate +
            ", ship = " + (ship != null) +
            ", hit = " + hit +
            "}";
    }
}
