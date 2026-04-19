package battleships_ex.gdx.model.board;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

public class Ship {
    private final ShipType type;
    private final Orientation orientation;
    private boolean placed;
    private Set<Coordinate> occupiedCoordinates;
    private final Set<Coordinate> hits;

    public Ship(ShipType type, Orientation orientation) {
        this.type = type;
        this.orientation = orientation;
        this.placed = false;
        this.occupiedCoordinates = new LinkedHashSet<>();
        this.hits = new HashSet<>();
    }

    public ShipType getType() {
        return type;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getLength() {
        return type.getLength();
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

    public Set<Coordinate> getOccupiedCoordinates() {
        return Collections.unmodifiableSet(occupiedCoordinates);
    }

    public void setOccupiedCoordinates(Set<Coordinate> occupiedCoordinates) {
        this.occupiedCoordinates = new LinkedHashSet<>(occupiedCoordinates);
    }

    public void place(Set<Coordinate> coordinates) {
        this.occupiedCoordinates = new LinkedHashSet<>(coordinates);
        this.placed = true;
    }

    public void registerHit(Coordinate coordinate) {
        if (!occupiedCoordinates.contains(coordinate)) {
            throw new IllegalArgumentException("Coordinate " + coordinate + " is not occupied by this ship.");
        }
        hits.add(coordinate);
    }

    public boolean isSunk() {
        if (!placed || occupiedCoordinates.isEmpty()) {
            return false;
        }
        return hits.size() >= occupiedCoordinates.size();
    }

    public Set<Coordinate> getHits() {
        return Collections.unmodifiableSet(hits);
    }
}
