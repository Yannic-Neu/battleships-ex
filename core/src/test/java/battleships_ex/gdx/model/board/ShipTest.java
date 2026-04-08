package battleships_ex.gdx.model.board;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    @Test
    void shipIsNotSunkWhenNoHits() {
        Ship ship = placedShip(ShipType.CRUISER, 0, 0); // length 2
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsNotSunkWithPartialHits() {
        Ship ship = placedShip(ShipType.CRUISER, 0, 0); // length 2, cells (0,0) and (0,1)
        ship.registerHit(new Coordinate(0, 0));
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsSunkWhenAllCellsHit() {
        // PATROL length 2 — must hit both cells
        Ship ship = placedShip(ShipType.PATROL, 0, 0); // cells (0,0) and (0,1)
        ship.registerHit(new Coordinate(0, 0));
        assertFalse(ship.isSunk());           // only 1 of 2 hit
        ship.registerHit(new Coordinate(0, 1));
        assertTrue(ship.isSunk());
    }

    @Test
    void shipIsSunkAfterAllCellsHitMultiCell() {
        // DESTROYER length 4 — cells (0,0), (0,1), (0,2), (0,3)
        Ship ship = placedShip(ShipType.CRUISER, 0, 0);
        ship.registerHit(new Coordinate(0, 0));
        ship.registerHit(new Coordinate(0, 1));
        assertFalse(ship.isSunk());           // 2 of 4 hit
        ship.registerHit(new Coordinate(0, 2));
        assertFalse(ship.isSunk());
        ship.registerHit(new Coordinate(0,3));
        assertTrue(ship.isSunk());
    }

    @Test
    void occupiedCoordinatesIsUnmodifiable() {
        Ship ship = placedShip(ShipType.PATROL, 3, 3);
        assertThrows(UnsupportedOperationException.class, () ->
            ship.getOccupiedCoordinates().add(new Coordinate(9, 9)));
    }

    @Test
    void registerHitOnUnoccupiedCoordinateThrows() {
        Ship ship = placedShip(ShipType.PATROL, 0, 0);
        assertThrows(IllegalArgumentException.class, () ->
            ship.registerHit(new Coordinate(9, 9)));
    }

    @Test
    void getLengthMatchesShipType() {
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.HORIZONTAL);
        assertEquals(ShipType.SUBMARINE.getLength(), ship.getLength());
    }

    @Test
    void isNotPlacedBeforePlacement() {
        Ship ship = new Ship(ShipType.CRUISER, Orientation.HORIZONTAL);
        assertFalse(ship.isPlaced());
    }

    @Test
    void isPlacedAfterPlacement() {
        Ship ship = placedShip(ShipType.CRUISER, 0, 0);
        assertTrue(ship.isPlaced());
    }

    // -------------------------------------------------------------------------
    // Helper — builds a Ship and places it manually (row, col = horizontal anchor)
    // -------------------------------------------------------------------------

    /**
     * Creates a ship with the given type and places it horizontally
     * starting at (row, col). Uses the same coordinate logic as Board.
     */
    private Ship placedShip(ShipType type, int row, int col) {
        Ship ship = new Ship(type, Orientation.HORIZONTAL);
        Set<Coordinate> coords = new LinkedHashSet<>();
        for (int i = 0; i < type.getLength(); i++) {
            coords.add(new Coordinate(row, col + i));
        }
        ship.place(coords);
        return ship;
    }
}
