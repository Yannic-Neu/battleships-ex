package battleships_ex.gdx.model.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ship placed on the board, occupying one or more cells.
 */
public class Ship {

    private final List<Cell> position;

    /**
     * Creates a ship occupying the given cells.
     * Note: the ship holds references to the Board's Cell instances so that
     * isSunk() reflects the live hit state. Do not substitute copies.
     */
    public Ship(List<Cell> cells) {
        this.position = new ArrayList<>(cells);
    }

    public List<Cell> getPosition() {
        return Collections.unmodifiableList(position);
    }

    /**
     * A ship is sunk when all of its cells have been hit.
     */
    public boolean isSunk() {
        for (Cell cell : position) {
            if (!cell.isHit()) {
                return false;
            }
        }
        return !position.isEmpty();
    }

    public int getSize() {
        return position.size();
    }
}
