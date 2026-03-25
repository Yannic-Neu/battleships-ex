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
        // Horizontal means same row, different columns
        assertTrue(board.placeShip(0, 0, 3, true));

        assertTrue(board.getCell(0, 0).hasShip());
        assertTrue(board.getCell(0, 1).hasShip());
        assertTrue(board.getCell(0, 2).hasShip());
        assertFalse(board.getCell(0, 3).hasShip());

        assertEquals(1, board.getShips().size());
        assertEquals(3, board.getShips().get(0).getLength());
    }

    @Test
    void placeShipVertically() {
        // Vertical means same column, different rows
        assertTrue(board.placeShip(0, 0, 4, false));

        assertTrue(board.getCell(0, 0).hasShip());
        assertTrue(board.getCell(1, 0).hasShip());
        assertTrue(board.getCell(2, 0).hasShip());
        assertTrue(board.getCell(3, 0).hasShip());
    }

    @Test
    void placeShipOutOfBoundsReturnsFalse() {
        // Horizontal starting at (0, 8) with length 4 goes to col 11, which is OOB
        assertFalse(board.placeShip(0, 8, 4, true));
        assertTrue(board.getShips().isEmpty());
    }

    @Test
    void placeShipOnOccupiedCellReturnsFalse() {
        assertTrue(board.placeShip(0, 0, 3, true));
        // Tries to place a vertical ship that overlaps with the horizontal one at (0, 0)
        assertFalse(board.placeShip(0, 0, 2, false));
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

        board.receiveAttack(new Coordinate(0, 1));
        assertTrue(board.allShipsSunk());
    }

    @Test
    void allShipsSunkWithNoShips() {
        assertFalse(board.allShipsSunk());
    }
}
