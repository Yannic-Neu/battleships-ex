package battleships_ex.gdx.model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the 10x10 game board containing cells and placed ships.
 */
public class Board {

    public static final int DEFAULT_SIZE = 10;

    private final Cell[][] grid;
    private final List<Ship> ships;

    public Board() {
        this(DEFAULT_SIZE);
    }

    public Board(int size) {
        grid = new Cell[size][size];
        ships = new ArrayList<>();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                grid[x][y] = new Cell(new Coordinate(x, y));
            }
        }
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getSize() {
        return grid.length;
    }

    public Cell getCell(int x, int y) {
        return grid[x][y];
    }

    public Cell getCell(Coordinate coordinate) {
        return grid[coordinate.getX()][coordinate.getY()];
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    /**
     * Places a ship on the board starting at the given coordinate.
     *
     * @param startX    column of the first cell
     * @param startY    row of the first cell
     * @param size      number of cells the ship occupies
     * @param horizontal true for left-to-right, false for top-to-bottom
     * @return true if the ship was placed successfully
     */
    public boolean placeShip(int startX, int startY, int size, boolean horizontal) {
        List<Cell> cells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            int x = horizontal ? startX + i : startX;
            int y = horizontal ? startY : startY + i;

            if (!isInBounds(x, y)) return false;
            if (grid[x][y].hasShip()) return false;

            cells.add(grid[x][y]);
        }

        for (Cell cell : cells) {
            cell.setHasShip(true);
        }

        ships.add(new Ship(cells));
        return true;
    }

    /**
     * Processes an incoming attack at the given coordinate.
     *
     * @param coordinate the target position
     * @return true if a ship was hit, false if it was a miss
     * @throws IllegalArgumentException if the coordinate is out of bounds
     * @throws IllegalStateException    if the cell was already hit
     */
    public boolean receiveAttack(Coordinate coordinate) {
        if (!isInBounds(coordinate.getX(), coordinate.getY())) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
        }

        Cell cell = getCell(coordinate);
        if (cell.isHit()) {
            throw new IllegalStateException("Cell already hit: " + coordinate);
        }

        cell.setHit(true);
        return cell.hasShip();
    }

    /**
     * @return true if all ships on this board have been sunk
     */
    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        for (Ship ship : ships) {
            if (!ship.isSunk()) return false;
        }
        return true;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid.length;
    }
}
