package battleships_ex.gdx;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.SessionManager;
import battleships_ex.gdx.model.board.Coordinate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionManager} session lifecycle, heartbeat, and timeout logic.
 * Uses a recording stub to verify interactions with GameDataSource.
 */
class SessionManagerTest {

    private RecordingGameDataSource dataSource;
    private SessionManager sessionManager;
    private RecordingSessionListener sessionListener;

    @BeforeEach
    void setUp() {
        dataSource = new RecordingGameDataSource();
        sessionManager = new SessionManager(dataSource);
        sessionListener = new RecordingSessionListener();
        sessionManager.setListener(sessionListener);
    }

    @Test
    void startSession_setsActiveState() {
        sessionManager.startSession("ROOM1", "player1", "player2");

        assertTrue(sessionManager.isSessionActive());
        assertEquals("ROOM1", sessionManager.getRoomCode());
        assertEquals("player1", sessionManager.getLocalPlayerId());
        assertEquals("player2", sessionManager.getOpponentPlayerId());
    }

    @Test
    void endSession_clearsActiveState() {
        sessionManager.startSession("ROOM1", "player1", "player2");
        sessionManager.endSession();

        assertFalse(sessionManager.isSessionActive());
    }

    @Test
    void startSession_registersHeartbeatListener() {
        sessionManager.startSession("ROOM1", "player1", "player2");

        assertTrue(dataSource.heartbeatListenerAdded);
        assertEquals("ROOM1", dataSource.lastHeartbeatRoom);
        assertEquals("player2", dataSource.lastHeartbeatOpponentId);
    }

    @Test
    void endSession_removesHeartbeatListener() {
        sessionManager.startSession("ROOM1", "player1", "player2");
        sessionManager.endSession();

        assertTrue(dataSource.heartbeatListenerRemoved);
    }

    @Test
    void isSessionActive_falseBeforeStart() {
        assertFalse(sessionManager.isSessionActive());
    }

    @Test
    void heartbeatDisconnect_notifiesListener() {
        sessionManager.startSession("ROOM1", "player1", "player2");

        // Simulate opponent disconnect via the captured callback
        assertNotNull(dataSource.heartbeatCallback);
        dataSource.heartbeatCallback.onSuccess(true); // true = disconnected

        assertTrue(sessionListener.opponentDisconnected);
    }

    @Test
    void heartbeatReconnect_notifiesListener() {
        sessionManager.startSession("ROOM1", "player1", "player2");

        // Simulate reconnect
        assertNotNull(dataSource.heartbeatCallback);
        dataSource.heartbeatCallback.onSuccess(false); // false = connected

        assertTrue(sessionListener.opponentReconnected);
    }

    @Test
    void endSession_preventsCallbacksAfterEnd() {
        sessionManager.startSession("ROOM1", "player1", "player2");
        DataCallback<Boolean> cb = dataSource.heartbeatCallback;

        sessionManager.endSession();

        // Simulate late callback - should not crash or trigger listener
        cb.onSuccess(true);
        assertFalse(sessionListener.opponentDisconnected);
    }

    // ── Recording stubs ─────────────────────────────────────────────

    private static class RecordingGameDataSource implements GameDataSource {
        boolean heartbeatListenerAdded = false;
        boolean heartbeatListenerRemoved = false;
        String lastHeartbeatRoom;
        String lastHeartbeatOpponentId;
        DataCallback<Boolean> heartbeatCallback;

        @Override
        public void submitMove(String roomCode, String playerId, Coordinate target, boolean hit, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override public void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback) {}
        @Override public void removeMoveListener(String roomCode) {}

        @Override
        public void submitActionCardPlay(String roomCode, String playerId, String cardName, Coordinate target, String metadata, DataCallback<Void> callback) {

        }

        @Override
        public void addActionCardListener(String roomCode, DataCallback<ActionCardSnapshot> callback) {

        }

        @Override
        public void removeActionCardListener(String roomCode) {

        }

        @Override
        public void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override public void addTurnListener(String roomCode, DataCallback<String> callback) {}
        @Override public void removeTurnListener(String roomCode) {}

        @Override
        public void updateGameStatus(String roomCode, String status, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override
        public void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override
        public void addPlacementStatusListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
            // No-op
        }

        @Override
        public void updateBoardLayout(String roomCode, String playerId, java.util.List<battleships_ex.gdx.data.ShipPlacement> ships, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override
        public void addBoardLayoutListener(String roomCode, String opponentId, DataCallback<java.util.List<battleships_ex.gdx.data.ShipPlacement>> callback) {
            // No-op
        }

        @Override
        public void removeBoardLayoutListener(String roomCode) {
            // No-op for recording stub
        }

        @Override
        public void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override public void sendHeartbeat(String roomCode, String playerId) {}

        @Override
        public void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
            heartbeatListenerAdded = true;
            lastHeartbeatRoom = roomCode;
            lastHeartbeatOpponentId = opponentId;
            heartbeatCallback = callback;
        }

        @Override
        public void removeHeartbeatListener(String roomCode) {
            heartbeatListenerRemoved = true;
        }

        @Override public void sendPreview(String roomCode, String playerId, Coordinate target) {}
        @Override public void clearPreview(String roomCode, String playerId) {}
        @Override public void addPreviewListener(String roomCode, String opponentId, DataCallback<Coordinate> callback) {}
        @Override public void removePreviewListener(String roomCode) {}

        @Override
        public void roomIsActive(String roomCode, DataCallback<Boolean> callback) {
            callback.onSuccess(false);
        }

        @Override
        public void loadGameState(String roomCode, DataCallback<GameSnapshot> callback) {
            callback.onSuccess(new GameSnapshot(null, "finished"));
        }

        @Override
        public void cleanupSession(String roomCode, DataCallback<Void> callback) {
            callback.onSuccess(null);
        }

        @Override
        public void removeAllListeners(String roomCode) {
            removeHeartbeatListener(roomCode);
        }
    }

    private static class RecordingSessionListener implements SessionManager.SessionListener {
        boolean opponentDisconnected = false;
        boolean opponentReconnected = false;
        boolean sessionTimedOut = false;

        @Override
        public void onOpponentDisconnected() {
            opponentDisconnected = true;
        }

        @Override
        public void onOpponentReconnected() {
            opponentReconnected = true;
        }

        @Override
        public void onSessionTimeout() {
            sessionTimedOut = true;
        }
    }
}
