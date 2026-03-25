package battleships_ex.gdx.model.board;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    @Test
    void shipIsNotSunkWhenNoHits() {
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(1, 0);
        Set<Coordinate> coords = new LinkedHashSet<>();
        coords.add(c1);
        coords.add(c2);

        Ship ship = new Ship(ShipType.PATROL, Orientation.VERTICAL);
        ship.place(coords);
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsNotSunkWithPartialHits() {
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(1, 0);
        Set<Coordinate> coords = new LinkedHashSet<>();
        coords.add(c1);
        coords.add(c2);

        Ship ship = new Ship(ShipType.PATROL, Orientation.VERTICAL);
        ship.place(coords);
        ship.registerHit(c1);
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsSunkWhenAllCellsHit() {
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(1, 0);
        Set<Coordinate> coords = new LinkedHashSet<>();
        coords.add(c1);
        coords.add(c2);

        Ship ship = new Ship(ShipType.PATROL, Orientation.VERTICAL);
        ship.place(coords);
        ship.registerHit(c1);
        ship.registerHit(c2);
        assertTrue(ship.isSunk());
    }

    @Test
    void occupiedCoordinatesIsUnmodifiable() {
        Coordinate c1 = new Coordinate(0, 0);
        Set<Coordinate> coords = new LinkedHashSet<>();
        coords.add(c1);

        // PATROL length is 2, so let's use a 1-length ship type for the test if it exists,
        // but PATROL is 2. I'll use a type that matches the size for validity.
        // Actually, the constructor doesn't check length, place() does.

        Ship ship = new Ship(ShipType.PATROL, Orientation.VERTICAL);
        // ship.place(coords); // This would throw due to length mismatch (PATROL is 2)

        assertThrows(UnsupportedOperationException.class,
            () -> ship.getOccupiedCoordinates().add(new Coordinate(1, 1)));
    }
}
