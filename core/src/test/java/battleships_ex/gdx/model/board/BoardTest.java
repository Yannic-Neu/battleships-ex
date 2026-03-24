package battleships_ex.gdx.model.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void boardInitializesWithCorrectSize() {
        assertEquals(10, board.getSize());
        assertNotNull(board.getCell(0, 0));
        assertNotNull(board.getCell(9, 9));
    }

    @Test
    void placeShipHorizontally() {
        assertTrue(board.placeShip(0, 0, 3, true));

        assertTrue(board.getCell(0, 0).hasShip());
        assertTrue(board.getCell(1, 0).hasShip());
        assertTrue(board.getCell(2, 0).hasShip());
        assertFalse(board.getCell(3, 0).hasShip());

        assertEquals(1, board.getShips().size());
        assertEquals(3, board.getShips().get(0).getSize());
    }

    @Test
    void placeShipVertically() {
        assertTrue(board.placeShip(0, 0, 4, false));

        assertTrue(board.getCell(0, 0).hasShip());
        assertTrue(board.getCell(0, 1).hasShip());
        assertTrue(board.getCell(0, 2).hasShip());
        assertTrue(board.getCell(0, 3).hasShip());
    }

    @Test
    void placeShipOutOfBoundsReturnsFalse() {
        assertFalse(board.placeShip(8, 0, 4, true));
        assertTrue(board.getShips().isEmpty());
    }

    @Test
    void placeShipOnOccupiedCellReturnsFalse() {
        assertTrue(board.placeShip(0, 0, 3, true));
        assertFalse(board.placeShip(1, 0, 2, false));
        assertEquals(1, board.getShips().size());
    }

    @Test
    void receiveAttackHit() {
        board.placeShip(0, 0, 2, true);

        assertTrue(board.receiveAttack(new Coordinate(0, 0)));
        assertTrue(board.getCell(0, 0).isHit());
    }

    @Test
    void receiveAttackMiss() {
        board.placeShip(0, 0, 2, true);

        assertFalse(board.receiveAttack(new Coordinate(5, 5)));
        assertTrue(board.getCell(5, 5).isHit());
    }

    @Test
    void receiveAttackOnAlreadyHitCellThrows() {
        board.receiveAttack(new Coordinate(0, 0));
        assertThrows(IllegalStateException.class,
            () -> board.receiveAttack(new Coordinate(0, 0)));
    }

    @Test
    void receiveAttackOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> board.receiveAttack(new Coordinate(10, 0)));
    }

    @Test
    void allShipsSunk() {
        board.placeShip(0, 0, 2, true);

        assertFalse(board.allShipsSunk());

        board.receiveAttack(new Coordinate(0, 0));
        assertFalse(board.allShipsSunk());

        board.receiveAttack(new Coordinate(1, 0));
        assertTrue(board.allShipsSunk());
    }

    @Test
    void allShipsSunkWithNoShips() {
        assertFalse(board.allShipsSunk());
    }
}
