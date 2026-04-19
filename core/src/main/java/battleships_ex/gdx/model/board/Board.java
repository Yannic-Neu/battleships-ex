package battleships_ex.gdx.model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

public class Board {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final List<Ship> ships;
    private final Set<Coordinate> mines = new java.util.HashSet<>();
    private final Set<Coordinate> scannedTiles = new java.util.HashSet<>();

    /**
     * Default board size
     */
    public Board() {
        this(10, 10);
    }

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

    public int getSize() {
        return width;
    }

    public Cell getCell(Coordinate coordinate) {
        validateCoordinate(coordinate);
        return grid[coordinate.getRow()][coordinate.getCol()];
    }

    public Cell getCell(int row, int col) {
        return getCell(new Coordinate(row, col));
    }

    public boolean isWithinBounds(Coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }

        return isWithinBounds(coordinate.getRow(), coordinate.getCol());
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
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

    public void removeShip(Ship ship) {
        if (ship == null || !ships.contains(ship)) {
            return;
        }

        for (Coordinate coordinate : ship.getOccupiedCoordinates()) {
            grid[coordinate.getRow()][coordinate.getCol()].removeShip();
        }
        ship.unplace();
        ships.remove(ship);
    }

    /**
     * Legacy helper method for placing ships.
     * Finds a matching ShipType for the given length and attempts placement.
     */
    public boolean placeShip(int row, int col, int length, boolean horizontal) {
        ShipType type = null;
        for (ShipType t : ShipType.values()) {
            if (t.getLength() == length) {
                type = t;
                break;
            }
        }
        if (type == null) return false;

        Orientation orientation = horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        Coordinate start = new Coordinate(row, col);
        Ship ship = new Ship(type, orientation);
        if (canPlaceShip(ship, start, orientation)) {
            placeShip(ship, start, orientation);
            return true;
        }
        return false;
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

    public boolean receiveAttack(Coordinate coordinate) {
        AttackResult result = attack(coordinate);
        if (result == AttackResult.ALREADY_HIT) {
            throw new IllegalStateException("Cell at " + coordinate + " has already been hit.");
        }
        return result == AttackResult.HIT || result == AttackResult.SUNK;
    }

    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        return ships.stream().allMatch(Ship::isSunk);
    }

    public void placeMine(Coordinate coord) {
        validateCoordinate(coord);
        mines.add(coord);
    }

    public boolean hasMine(Coordinate coord) {
        return mines.contains(coord);
    }

    public void removeMine(Coordinate coord) {
        mines.remove(coord);
    }

    public java.util.Set<Coordinate> getMines() {
        return java.util.Collections.unmodifiableSet(mines);
    }

    public void markScanned(Coordinate coord) {
        validateCoordinate(coord);
        scannedTiles.add(coord);
    }

    public boolean hasBeenScanned(Coordinate coord) {
        return scannedTiles.contains(coord);
    }

    public void clearScannedTiles() {
        scannedTiles.clear();
    }

    public List<Coordinate> getUnhitTiles() {
        List<Coordinate> unhit = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!grid[row][col].isHit()) {
                    unhit.add(grid[row][col].getCoordinate());
                }
            }
        }
        return unhit;
    }

    public List<Coordinate> getUnoccupiedTiles() {
        List<Coordinate> unoccupied = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = grid[row][col];
                if (!cell.hasShip() && !hasMine(cell.getCoordinate())) {
                    unoccupied.add(cell.getCoordinate());
                }
            }
        }
        return unoccupied;
    }

    public List<Coordinate> getAdjacentTiles(Coordinate center) {
        List<Coordinate> adjacent = new ArrayList<>();
        for (int r = center.getRow() - 1; r <= center.getRow() + 1; r++) {
            for (int c = center.getCol() - 1; c <= center.getCol() + 1; c++) {
                if (r == center.getRow() && c == center.getCol()) continue;
                if (isWithinBounds(r, c)) {
                    adjacent.add(new Coordinate(r, c));
                }
            }
        }
        return adjacent;
    }

    public int countAdjacentOccupancy(Coordinate center) {
        int count = 0;
        for (Coordinate adj : getAdjacentTiles(center)) {
            Cell cell = getCell(adj);
            if (cell.hasShip() || hasMine(adj)) {
                count++;
            }
        }
        return count;
    }

    private List<Coordinate> getPlacementCoordinates(Ship ship, Coordinate start, Orientation orientation) {
        List<Coordinate> coordinates = new ArrayList<>();

        for (int i = 0; i < ship.getLength(); i++) {
            int r = start.getRow();
            int c = start.getCol();

            if (orientation == Orientation.HORIZONTAL) {
                c += i;
            } else {
                r += i;
            }

            coordinates.add(new Coordinate(r, c));
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

    /**
     * Checks if there are any cells on the board that have not been attacked yet.
     *
     * @return true if there is at least one cell that has not been hit, false otherwise.
     */
    public boolean hasValidTargets() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!grid[row][col].isHit()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves a list of all coordinates on the board that have not yet been attacked.
     *
     * @return a {@code List} of {@link Coordinate} objects representing the cells that are not hit.
     */
    public List<Coordinate> getValidTargets() {
        List<Coordinate> validTargets = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = grid[row][col];
                if (!cell.isHit()) {
                    validTargets.add(cell.getCoordinate());
                }
            }
        }
        return validTargets;
    }
}
