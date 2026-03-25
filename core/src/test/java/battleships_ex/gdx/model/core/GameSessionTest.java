package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private Player player1;
    private Player player2;
    private GameSession session;

    @BeforeEach
    void setUp() {
        player1 = new Player(1, "Alice");
        player2 = new Player(2, "Bob");
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
            () -> session.processMove(new Coordinate(0, 0)));
    }

    @Test
    void missedShotSwitchesTurn() {
        session.startGame();
        // No ships on player2's board initially
        player2.getBoard().placeShip(5, 5, 2, true);

        Move move = session.processMove(new Coordinate(0, 0));
        assertFalse(move.isHit());
        assertEquals(player2, session.getCurrentPlayer());
    }

    @Test
    void hitDoesNotSwitchTurn() {
        session.startGame();
        player2.getBoard().placeShip(0, 0, 2, true);

        Move move = session.processMove(new Coordinate(0, 0));
        assertTrue(move.isHit());
        assertEquals(player1, session.getCurrentPlayer());
    }

    @Test
    void gameOverWhenAllShipsSunk() {
        session.startGame();
        // Use PATROL (length 2) since length 1 might not be supported by legacy placeShip
        player2.getBoard().placeShip(0, 0, 2, true);

        assertFalse(session.gameIsOver());

        session.processMove(new Coordinate(0, 0));
        assertFalse(session.gameIsOver());

        session.processMove(new Coordinate(0, 1));
        assertTrue(session.gameIsOver());
        assertEquals(player1, session.getWinner());
    }

    @Test
    void moveAfterGameOverThrows() {
        session.startGame();
        player2.getBoard().placeShip(0, 0, 2, true);
        session.processMove(new Coordinate(0, 0));
        session.processMove(new Coordinate(0, 1));

        assertThrows(IllegalStateException.class,
            () -> session.processMove(new Coordinate(1, 1)));
    }

    @Test
    void moveHistoryTracksAllMoves() {
        session.startGame();
        player2.getBoard().placeShip(5, 5, 2, true);

        session.processMove(new Coordinate(0, 0)); // miss -> switches to player2
        player1.getBoard().placeShip(5, 5, 2, true);
        session.processMove(new Coordinate(0, 0)); // miss -> switches to player1

        assertEquals(2, session.getMoveHistory().size());
    }
}
