package battleships_ex.gdx;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.StubGameDataSource;
import battleships_ex.gdx.model.board.Coordinate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Interface-contract tests for {@link GameDataSource} using the
 * {@link StubGameDataSource} implementation.
 */
class GameDataSourceTest {

    private StubGameDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new StubGameDataSource();
    }

    @Test
    void submitMove_callsOnSuccess() {
        boolean[] called = {false};

        dataSource.submitMove("ROOM1", "p1", new Coordinate(3, 5), true, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                called[0] = true;
            }

            @Override
            public void onFailure(String error) {
                fail("Should not fail");
            }
        });

        assertTrue(called[0]);
    }

    @Test
    void syncTurn_callsOnSuccess() {
        boolean[] called = {false};

        dataSource.syncTurn("ROOM1", "player1", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                called[0] = true;
            }

            @Override
            public void onFailure(String error) {
                fail("Should not fail");
            }
        });

        assertTrue(called[0]);
    }

    @Test
    void updateGameStatus_callsOnSuccess() {
        boolean[] called = {false};

        dataSource.updateGameStatus("ROOM1", "playing", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                called[0] = true;
            }

            @Override
            public void onFailure(String error) {
                fail("Should not fail");
            }
        });

        assertTrue(called[0]);
    }

    @Test
    void pushGameOver_callsOnSuccess() {
        boolean[] called = {false};

        dataSource.pushGameOver("ROOM1", "Player1", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                called[0] = true;
            }

            @Override
            public void onFailure(String error) {
                fail("Should not fail");
            }
        });

        assertTrue(called[0]);
    }

    @Test
    void moveSnapshot_holdsCorrectValues() {
        GameDataSource.MoveSnapshot snap = new GameDataSource.MoveSnapshot("p1", 4, 7, true, 12345L);

        assertEquals("p1", snap.playerId);
        assertEquals(4, snap.row);
        assertEquals(7, snap.col);
        assertTrue(snap.hit);
        assertEquals(12345L, snap.timestamp);
    }

    @Test
    void gameSnapshot_holdsCorrectValues() {
        GameDataSource.GameSnapshot snap = new GameDataSource.GameSnapshot("player1", "playing");

        assertEquals("player1", snap.currentTurn);
        assertEquals("playing", snap.status);
    }

    @Test
    void listenerMethods_doNotThrow() {
        // Stub methods should not throw
        assertDoesNotThrow(() -> dataSource.addMoveListener("ROOM1", new DataCallback<GameDataSource.MoveSnapshot>() {
            @Override public void onSuccess(GameDataSource.MoveSnapshot result) {}
            @Override public void onFailure(String error) {}
        }));
        assertDoesNotThrow(() -> dataSource.removeMoveListener("ROOM1"));
        assertDoesNotThrow(() -> dataSource.addTurnListener("ROOM1", new DataCallback<String>() {
            @Override public void onSuccess(String result) {}
            @Override public void onFailure(String error) {}
        }));
        assertDoesNotThrow(() -> dataSource.removeTurnListener("ROOM1"));
        assertDoesNotThrow(() -> dataSource.sendHeartbeat("ROOM1", "p1"));
        assertDoesNotThrow(() -> dataSource.addHeartbeatListener("ROOM1", "p2", new DataCallback<Boolean>() {
            @Override public void onSuccess(Boolean result) {}
            @Override public void onFailure(String error) {}
        }));
        assertDoesNotThrow(() -> dataSource.removeHeartbeatListener("ROOM1"));
        assertDoesNotThrow(() -> dataSource.removeAllListeners("ROOM1"));
    }
}
