package battleships_ex.gdx.model.core;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.rules.ShotResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private Player      player1;
    private Player      player2;
    private GameSession session;

    @BeforeEach
    void setUp() {
        // Player now uses String ids — matches LobbyDataSource and Firebase
        player1 = new Player("1", "Alice");
        player2 = new Player("2", "Bob");
        session = new GameSession(player1, player2);
    }

    @Test
    void sessionStartsWithPlayer1() {
        session.startGame();
        assertEquals(player1, session.getCurrentPlayer());
    }

    @Test
    void processMoveBeforeStartThrows() {
        assertThrows(IllegalStateException.class,
            () -> session.processMove(new Coordinate(0, 0), ShotResult.miss(new Coordinate(0, 0))));
    }

    @Test
    void missedShotSwitchesTurn() {
        session.startGame();
        // Place a ship so allShipsSunk() can track state; fire at a cell with no ship
        placeShip(player2, ShipType.CRUISER, 5, 5);

        Coordinate coord = new Coordinate(0, 0);
        Move move = session.processMove(coord, ShotResult.miss(coord)); // miss
        assertFalse(move.isHit());
        assertEquals(player2, session.getCurrentPlayer());
    }

    @Test
    void hitDoesNotSwitchTurn() {
        session.startGame();
        placeShip(player2, ShipType.CRUISER, 0, 0); // occupies (0,0) and (0,1)

        Coordinate coord = new Coordinate(0, 0);
        Move move = session.processMove(coord, ShotResult.hit(coord, player2.getBoard().getCell(coord).getShip())); // hit
        assertTrue(move.isHit());
        assertEquals(player1, session.getCurrentPlayer()); // turn stays with attacker
    }

    @Test
    void gameOverWhenAllShipsSunk() {
        session.startGame();
        // PATROL length 2 — both cells must be hit
        placeShip(player2, ShipType.PATROL, 0, 0); // occupies (0,0) and (0,1)

        assertFalse(session.gameIsOver());
        Coordinate c1 = new Coordinate(0, 0);
        player2.getBoard().attack(c1);
        session.processMove(c1, ShotResult.hit(c1, player2.getBoard().getCell(c1).getShip())); // HIT — turn stays
        assertFalse(session.gameIsOver());
        Coordinate c2 = new Coordinate(0, 1);
        player2.getBoard().attack(c2);
        session.processMove(c2, ShotResult.sunk(c2, player2.getBoard().getCell(c2).getShip())); // SUNK — game over
        assertTrue(session.gameIsOver());
        assertEquals(player1, session.getWinner());
    }

    @Test
    void moveHistoryTracksAllMoves() {
        session.startGame();
        placeShip(player2, ShipType.CRUISER, 5, 5);
        placeShip(player1, ShipType.CRUISER, 5, 5);

        Coordinate c1 = new Coordinate(0, 0);
        session.processMove(c1, ShotResult.miss(c1)); // miss → switches to player2
        session.processMove(c1, ShotResult.miss(c1)); // miss → switches back to player1

        assertEquals(2, session.getMoveHistory().size());
    }

    @Test
    void sunkShotKeepsCurrentTurn() {
        session.startGame();
        // Two PATROL ships — sink first fully before the game ends
        placeShip(player2, ShipType.PATROL, 0, 0); // cells (0,0) and (0,1)
        placeShip(player2, ShipType.PATROL, 5, 5); // cells (5,5) and (5,6)

        Coordinate c1 = new Coordinate(0, 0);
        player2.getBoard().attack(c1);
        session.processMove(c1, ShotResult.hit(c1, player2.getBoard().getCell(c1).getShip())); // HIT — turn stays
        Coordinate c2 = new Coordinate(0, 1);
        player2.getBoard().attack(c2);
        session.processMove(c2, ShotResult.sunk(c2, player2.getBoard().getCell(c2).getShip())); // SUNK — turn stays (hit-again rule)
        assertEquals(player1, session.getCurrentPlayer());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void placeShip(Player player, ShipType type, int row, int col) {
        Ship ship = new Ship(type, Orientation.HORIZONTAL);
        player.getBoard().placeShip(ship, new Coordinate(row, col), Orientation.HORIZONTAL);
    }
}
