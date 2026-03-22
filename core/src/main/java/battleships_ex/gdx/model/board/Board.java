package battleships_ex.gdx.model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;

public class Board {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final List<Ship> ships;

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Board width and height must be positive.");
        }

        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        this.ships = new ArrayList<>();

        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = new Cell(new Coordinate(row, col));
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(Coordinate coordinate) {
        validateCoordinate(coordinate);
        return grid[coordinate.getRow()][coordinate.getCol()];
    }

    public boolean isWithinBounds(Coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }

        return coordinate.getRow() >= 0
            && coordinate.getRow() < height
            && coordinate.getCol() >= 0
            && coordinate.getCol() < width;
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public boolean canPlaceShip(Ship ship, Coordinate start, Orientation orientation) {
        if (ship == null || start == null || orientation == null) {
            return false;
        }

        if (ship.isPlaced()) {
            return false;
        }

        List<Coordinate> coordinates = getPlacementCoordinates(ship, start, orientation);

        for (Coordinate coordinate : coordinates) {
            if (!isWithinBounds(coordinate)) {
                return false;
            }

            Cell cell = getCell(coordinate);
            if (cell.hasShip()) {
                return false;
            }
        }

        return true;
    }

    public void placeShip(Ship ship, Coordinate start, Orientation orientation) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null.");
        }
        if (start == null) {
            throw new IllegalArgumentException("Start coordinate must not be null.");
        }
        if (orientation == null) {
            throw new IllegalArgumentException("Orientation must not be null.");
        }
        if (!canPlaceShip(ship, start, orientation)) {
            throw new IllegalArgumentException("Ship cannot be placed at " + start + " with orientation " + orientation);
        }

        List<Coordinate> coordinates = getPlacementCoordinates(ship, start, orientation);
        Set<Coordinate> occupied = new LinkedHashSet<>(coordinates);

        ship.setOrientation(orientation);
        ship.place(occupied);

        for (Coordinate coordinate : coordinates) {
            getCell(coordinate).placeShip(ship);
        }

        ships.add(ship);
    }

    public AttackResult attack(Coordinate coordinate) {
        validateCoordinate(coordinate);

        Cell cell = getCell(coordinate);

        if (cell.isHit()) {
            return AttackResult.ALREADY_HIT;
        }

        cell.markHit();

        if (!cell.hasShip()) {
            return AttackResult.MISS;
        }

        Ship ship = cell.getShip();
        ship.registerHit(coordinate);

        if (ship.isSunk()) {
            return AttackResult.SUNK;
        }

        return AttackResult.HIT;
    }

    public boolean allShipsSunk() {
        return !ships.isEmpty() && ships.stream().allMatch(Ship::isSunk);
    }

    private List<Coordinate> getPlacementCoordinates(Ship ship, Coordinate start, Orientation orientation) {
        List<Coordinate> coordinates = new ArrayList<>();

        for (int i = 0; i < ship.getLength(); i++) {
            int row = start.getRow();
            int col = start.getCol();

            if (orientation == Orientation.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }

            coordinates.add(new Coordinate(row, col));
        }

        return coordinates;
    }

    private void validateCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate must not be null.");
        }
        if (!isWithinBounds(coordinate)) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
        }
    }
}
