package battleships_ex.gdx.data;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * Interface for game-state backend operations.
 * Abstracts real-time game synchronization so Firebase can be swapped
 * for another backend without affecting controllers or UI.
 *
 * Follows the same pattern as {@link LobbyDataSource}.
 *
 * DB structure (Firebase reference implementation):
 * <pre>
 * rooms/{roomCode}/
 *   game/
 *     currentTurn:   String (playerId)
 *     status:        "placing" | "playing" | "finished"
 *     moves/
 *       {pushId}:    { playerId: String, row: int, col: int, hit: boolean, timestamp: long }
 *     heartbeat/
 *       {playerId}:  { lastSeen: long }
 * </pre>
 */
public interface GameDataSource {

    // ── Move synchronization ────────────────────────────────────────

    /**
     * Submits a local move to the backend.
     *
     * @param roomCode  the active room code
     * @param playerId  the player who fired the shot
     * @param target    the coordinate that was targeted
     * @param hit       true if the shot was a hit, false if miss
     * @param callback  success: null / failure: error message
     */
    void submitMove(String roomCode, String playerId, Coordinate target, boolean hit, DataCallback<Void> callback);

    /**
     * Registers a listener for incoming opponent moves.
     * The callback fires each time a new move is pushed to the backend.
     *
     * @param roomCode  the active room code
     * @param callback  delivers {@link MoveSnapshot} for each new move
     */
    void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback);

    /**
     * Removes the move listener for the given room.
     */
    void removeMoveListener(String roomCode);

    // ── Turn synchronization ────────────────────────────────────────

    /**
     * Updates the current turn holder in the backend.
     *
     * @param roomCode       the active room code
     * @param currentPlayerId the player whose turn it now is
     * @param callback       success: null / failure: error message
     */
    void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback);

    /**
     * Registers a listener for turn changes.
     *
     * @param roomCode  the active room code
     * @param callback  delivers the playerId of the new turn holder
     */
    void addTurnListener(String roomCode, DataCallback<String> callback);

    /**
     * Removes the turn listener for the given room.
     */
    void removeTurnListener(String roomCode);

    // ── Game status ─────────────────────────────────────────────────

    /**
     * Updates the game status in the backend.
     *
     * @param roomCode  the active room code
     * @param status    one of "placing", "playing", "finished"
     * @param callback  success: null / failure: error message
     */
    void updateGameStatus(String roomCode, String status, DataCallback<Void> callback);

    /**
     * Signals game over with the winner's name.
     *
     * @param roomCode   the active room code
     * @param winnerName display name of the winner
     * @param callback   success: null / failure: error message
     */
    void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback);

    // ── Heartbeat / session health ──────────────────────────────────

    /**
     * Sends a heartbeat timestamp for the given player.
     * Should be called periodically (e.g. every 5 seconds).
     *
     * @param roomCode the active room code
     * @param playerId the local player's id
     */
    void sendHeartbeat(String roomCode, String playerId);

    /**
     * Registers a listener that fires when the opponent's heartbeat
     * becomes stale (> threshold seconds old).
     *
     * @param roomCode   the active room code
     * @param opponentId the opponent's player id
     * @param callback   delivers true when opponent is considered disconnected,
     *                   false when they reconnect
     */
    void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback);

    /**
     * Removes the heartbeat listener for the given room.
     */
    void removeHeartbeatListener(String roomCode);

    // ── Cleanup ─────────────────────────────────────────────────────

    /**
     * Removes all game-related listeners for the given room.
     * Should be called when leaving a game session.
     */
    void removeAllListeners(String roomCode);

    // ── Snapshot classes ────────────────────────────────────────────

    /**
     * Represents a single move received from the backend.
     */
    class MoveSnapshot {
        public final String playerId;
        public final int row;
        public final int col;
        public final boolean hit;
        public final long timestamp;

        public MoveSnapshot(String playerId, int row, int col, boolean hit, long timestamp) {
            this.playerId  = playerId;
            this.row       = row;
            this.col       = col;
            this.hit       = hit;
            this.timestamp = timestamp;
        }
    }

    /**
     * Represents the full game state snapshot for session recovery.
     */
    class GameSnapshot {
        public final String currentTurn;
        public final String status;

        public GameSnapshot(String currentTurn, String status) {
            this.currentTurn = currentTurn;
            this.status      = status;
        }
    }
}
