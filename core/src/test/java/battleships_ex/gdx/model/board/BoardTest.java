package battleships_ex.gdx.model.board;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 10);
    }

    @Test
    void boardInitializesWithCorrectDimensions() {
        assertEquals(10, board.getWidth());
        assertEquals(10, board.getHeight());
        assertNotNull(board.getCell(new Coordinate(0, 0)));
        assertNotNull(board.getCell(new Coordinate(9, 9)));
    }

    @Test
    void placeShipHorizontally() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL); // length 4
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertTrue(board.getCell(new Coordinate(0, 0)).hasShip());
        assertTrue(board.getCell(new Coordinate(0, 1)).hasShip());
        assertTrue(board.getCell(new Coordinate(0, 2)).hasShip());
        assertTrue(board.getCell(new Coordinate(0, 3)).hasShip());
        assertFalse(board.getCell(new Coordinate(0,4)).hasShip());

        assertEquals(1, board.getShips().size());
        assertEquals(4, board.getShips().get(0).getLength());
    }

    @Test
    void placeShipVertically() {
        Ship ship = new Ship(ShipType.CARRIER, Orientation.VERTICAL); // length 4
        board.placeShip(ship, new Coordinate(0, 0), Orientation.VERTICAL);

        assertTrue(board.getCell(new Coordinate(0, 0)).hasShip());
        assertTrue(board.getCell(new Coordinate(1, 0)).hasShip());
        assertTrue(board.getCell(new Coordinate(2, 0)).hasShip());
        assertTrue(board.getCell(new Coordinate(3, 0)).hasShip());
    }

    @Test
    void placeShipOutOfBoundsThrows() {
        // BATTLESHIP length 4 starting at col 8 → cols 8,9,10,11 — overshoots
        Ship ship = new Ship(ShipType.CARRIER, Orientation.HORIZONTAL);
        assertFalse(board.canPlaceShip(ship, new Coordinate(0, 8), Orientation.HORIZONTAL));
        assertThrows(IllegalArgumentException.class, () ->
            board.placeShip(ship, new Coordinate(0, 8), Orientation.HORIZONTAL));
        assertTrue(board.getShips().isEmpty());
    }

    @Test
    void placeShipOnOccupiedCellThrows() {
        Ship first  = new Ship(ShipType.SUBMARINE, Orientation.HORIZONTAL);
        Ship second = new Ship(ShipType.DESTROYER, Orientation.VERTICAL);
        board.placeShip(first, new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertFalse(board.canPlaceShip(second, new Coordinate(0, 0), Orientation.VERTICAL));
        assertThrows(IllegalArgumentException.class, () ->
            board.placeShip(second, new Coordinate(0, 0), Orientation.VERTICAL));
        assertEquals(1, board.getShips().size());
    }

    @Test
    void attackHitReturnsHit() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL); // length 2
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        AttackResult result = board.attack(new Coordinate(0, 0));
        assertEquals(AttackResult.HIT, result);
        assertTrue(board.getCell(new Coordinate(0, 0)).isHit());
    }

    @Test
    void attackMissReturnsMiss() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        AttackResult result = board.attack(new Coordinate(5, 5));
        assertEquals(AttackResult.MISS, result);
        assertTrue(board.getCell(new Coordinate(5, 5)).isHit());
    }

    @Test
    void attackAlreadyHitCellReturnsAlreadyHit() {
        board.attack(new Coordinate(0, 0));
        AttackResult result = board.attack(new Coordinate(0, 0));
        assertEquals(AttackResult.ALREADY_HIT, result);
    }

    @Test
    void attackOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            board.attack(new Coordinate(10, 0)));
    }

    @Test
    void attackSinksShipReturnsSunk() {
        // PATROL length 2 — hit both cells, second shot returns SUNK
        Ship ship = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        board.attack(new Coordinate(0, 0));                          // HIT
        AttackResult result = board.attack(new Coordinate(0, 1));    // SUNK
        assertEquals(AttackResult.SUNK, result);
        assertTrue(ship.isSunk());
    }

    @Test
    void allShipsSunkReturnsTrueWhenAllSunk() {
        // PATROL length 2 — both cells must be hit
        Ship ship = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertFalse(board.allShipsSunk());
        board.attack(new Coordinate(0, 0));
        assertFalse(board.allShipsSunk());   // only 1 of 2 cells hit
        board.attack(new Coordinate(0, 1));
        assertTrue(board.allShipsSunk());
    }

    @Test
    void allShipsSunkWithNoShipsReturnsFalse() {
        assertFalse(board.allShipsSunk());
    }
}
