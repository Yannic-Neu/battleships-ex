package battleships_ex.gdx.ui;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import battleships_ex.gdx.config.Orientation;
import battleships_ex.gdx.config.ShipType;

public class Ship {
    private final ShipType type;
    private Orientation orientation;
    private Set<Coordinate> occupiedCoordinates;
    private final Set<Coordinate> hitCoordinates;

    public Ship(ShipType type, Orientation orientation) {
        if (type == null) {
            throw new IllegalArgumentException("Ship type must not be null.");
        }
        if (orientation == null) {
            throw new IllegalArgumentException("Orientation must not be null.");
        }
        this.type = type;
        this.orientation = orientation;
        this.occupiedCoordinates = new LinkedHashSet<>();
        this.hitCoordinates = new LinkedHashSet<>();
    }
    public ShipType getType() {
        return type;
    }

    public String getName() {
        return type.getDisplayName();
    }

    public int getLength() {
        return type.getLength();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("Orientation must not be null.");
        }
        this.orientation = orientation;
    }

    public boolean isPlaced() {
        return !occupiedCoordinates.isEmpty();
    }

    public void place(Set<Coordinate> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates must not be null.");
        }
        if (isPlaced()) {
            throw new IllegalStateException("Ship has already been placed.");
        }
        if (coordinates.size() != getLength()) {
            throw new IllegalArgumentException(
                "Ship of type " + getName() + " must occupy exactly " + getLength() + " coordinates."
            );
        }
        if (coordinates.contains(null)) {
            throw new IllegalArgumentException("Coordinates must not contain null.");
        }

        this.occupiedCoordinates = new LinkedHashSet<>(coordinates);
    }

    public Set<Coordinate> getOccupiedCoordinates() {
        return Collections.unmodifiableSet(occupiedCoordinates);
    }

    public boolean occupies(Coordinate coordinate) {
        return occupiedCoordinates.contains(coordinate);
    }

    public void registerHit(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate must not be null.");
        }
        if (!occupies(coordinate)) {
            throw new IllegalArgumentException("Ship does not occupy coordinate " + coordinate);
        }

        hitCoordinates.add(coordinate);
    }

    public boolean isSunk() {
        return isPlaced() && hitCoordinates.size() == occupiedCoordinates.size();
    }
}
