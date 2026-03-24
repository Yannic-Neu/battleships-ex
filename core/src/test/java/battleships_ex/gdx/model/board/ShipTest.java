package battleships_ex.gdx.model.board;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    @Test
    void shipIsNotSunkWhenNoHits() {
        Cell c1 = new Cell(new Coordinate(0, 0));
        Cell c2 = new Cell(new Coordinate(1, 0));
        c1.setHasShip(true);
        c2.setHasShip(true);

        Ship ship = new Ship(Arrays.asList(c1, c2));
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsNotSunkWithPartialHits() {
        Cell c1 = new Cell(new Coordinate(0, 0));
        Cell c2 = new Cell(new Coordinate(1, 0));
        c1.setHasShip(true);
        c2.setHasShip(true);
        c1.setHit(true);

        Ship ship = new Ship(Arrays.asList(c1, c2));
        assertFalse(ship.isSunk());
    }

    @Test
    void shipIsSunkWhenAllCellsHit() {
        Cell c1 = new Cell(new Coordinate(0, 0));
        Cell c2 = new Cell(new Coordinate(1, 0));
        c1.setHasShip(true);
        c2.setHasShip(true);
        c1.setHit(true);
        c2.setHit(true);

        Ship ship = new Ship(Arrays.asList(c1, c2));
        assertTrue(ship.isSunk());
    }

    @Test
    void positionListIsUnmodifiable() {
        Cell c1 = new Cell(new Coordinate(0, 0));
        Ship ship = new Ship(Arrays.asList(c1));
        assertThrows(UnsupportedOperationException.class,
            () -> ship.getPosition().add(new Cell(new Coordinate(1, 1))));
    }
}
