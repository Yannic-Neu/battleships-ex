package battleships_ex.gdx.model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;

public class Board {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final List<Ship> ships;
    private final Set<Coordinate> mines = new HashSet<>();
    private final Set<Coordinate> scannedTiles = new HashSet<>();

    public Board() {
        this(10, 10);
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        this.ships = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = new Cell(new Coordinate(row, col));
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Cell getCell(Coordinate coordinate) {
        validateCoordinate(coordinate);
        return grid[coordinate.getRow()][coordinate.getCol()];
    }

    public boolean isWithinBounds(Coordinate coordinate) {
        if (coordinate == null) return false;
        return isWithinBounds(coordinate.getRow(), coordinate.getCol());
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public boolean canPlaceShip(Ship ship, Coordinate start, Orientation orientation) {
        if (ship == null || start == null || orientation == null) return false;
        List<Coordinate> coordinates = getPlacementCoordinates(ship, start, orientation);
        if (coordinates.isEmpty()) return false;
        for (Coordinate coord : coordinates) {
            if (!isWithinBounds(coord) || getCell(coord).hasShip()) return false;
        }
        return true;
    }

    public void placeShip(Ship ship, Coordinate start, Orientation orientation) {
        if (!canPlaceShip(ship, start, orientation)) throw new IllegalArgumentException("Invalid ship placement.");
        List<Coordinate> coordinates = getPlacementCoordinates(ship, start, orientation);
        for (Coordinate coord : coordinates) getCell(coord).setShip(ship);
        ship.setOccupiedCoordinates(new LinkedHashSet<>(coordinates));
        ship.setPlaced(true);
        ships.add(ship);
    }

    public void removeShip(Ship ship) {
        if (ship == null || !ships.contains(ship)) return;
        for (Coordinate coord : ship.getOccupiedCoordinates()) getCell(coord).setShip(null);
        ship.setPlaced(false);
        ship.setOccupiedCoordinates(new LinkedHashSet<>());
        ships.remove(ship);
    }

    public AttackResult attack(Coordinate coordinate) {
        validateCoordinate(coordinate);
        Cell cell = getCell(coordinate);
        if (cell.isHit()) return AttackResult.ALREADY_HIT;
        
        cell.setHit(true);
        if (cell.hasShip()) {
            Ship ship = cell.getShip();
            ship.registerHit(coordinate);
            if (ship.isSunk()) return AttackResult.SUNK;
            return AttackResult.HIT;
        }
        return AttackResult.MISS;
    }

    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        for (Ship ship : ships) {
            if (!ship.isSunk()) return false;
        }
        return true;
    }

    public void placeMine(Coordinate coord) {
        validateCoordinate(coord);
        mines.add(coord);
    }

    public boolean hasMine(Coordinate coord) { return mines.contains(coord); }
    public void removeMine(Coordinate coord) { mines.remove(coord); }
    public Set<Coordinate> getMines() { return Collections.unmodifiableSet(mines); }

    public void markScanned(Coordinate coord) {
        validateCoordinate(coord);
        scannedTiles.add(coord);
    }

    public Set<Coordinate> getScannedTiles() {
        return Collections.unmodifiableSet(scannedTiles);
    }

    public boolean hasBeenScanned(Coordinate coord) {
        return scannedTiles.contains(coord);
    }

    public void clearScannedTiles() {
        scannedTiles.clear();
    }

    public List<Coordinate> getUnhitTiles() {
        List<Coordinate> unhit = new ArrayList<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (!grid[r][c].isHit()) unhit.add(grid[r][c].getCoordinate());
            }
        }
        return unhit;
    }

    public List<Coordinate> getUnoccupiedTiles() {
        List<Coordinate> unoccupied = new ArrayList<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Coordinate coord = grid[r][c].getCoordinate();
                if (!grid[r][c].hasShip() && !hasMine(coord)) unoccupied.add(coord);
            }
        }
        return unoccupied;
    }

    public List<Coordinate> getAdjacentTiles(Coordinate center) {
        List<Coordinate> adjacent = new ArrayList<>();
        for (int r = center.getRow() - 1; r <= center.getRow() + 1; r++) {
            for (int c = center.getCol() - 1; c <= center.getCol() + 1; c++) {
                if (r == center.getRow() && c == center.getCol()) continue;
                if (isWithinBounds(r, c)) adjacent.add(new Coordinate(r, c));
            }
        }
        return adjacent;
    }

    public int countAdjacentOccupancy(Coordinate center) {
        int count = 0;
        for (int r = center.getRow() - 1; r <= center.getRow() + 1; r++) {
            for (int c = center.getCol() - 1; c <= center.getCol() + 1; c++) {
                if (isWithinBounds(r, c)) {
                    Coordinate coord = new Coordinate(r, c);
                    Cell cell = getCell(coord);
                    // Count structures (Ship or Mine) that are NOT YET HIT
                    if (!cell.isHit() && (cell.hasShip() || hasMine(coord))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private List<Coordinate> getPlacementCoordinates(Ship ship, Coordinate start, Orientation orientation) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < ship.getLength(); i++) {
            int r = start.getRow(); int c = start.getCol();
            if (orientation == Orientation.HORIZONTAL) c += i; else r += i;
            coordinates.add(new Coordinate(r, c));
        }
        return coordinates;
    }

    private void validateCoordinate(Coordinate coordinate) {
        if (!isWithinBounds(coordinate)) throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
    }
}
