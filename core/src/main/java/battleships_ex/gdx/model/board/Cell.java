package battleships_ex.gdx.model.board;

import battleships_ex.gdx.model.board.Ship;

public class Cell {
    private final Coordinate coordinate;
    private boolean hit;
    private Ship ship;

    public Cell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.hit = false;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean hasShip() {
        return ship != null;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }
}
