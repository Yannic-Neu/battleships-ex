package battleships_ex.gdx.data;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * Interface for game-state backend operations.
 */
public interface GameDataSource {

    void submitMove(String roomCode, String playerId, Coordinate target, boolean hit, DataCallback<Void> callback);
    void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback);
    void removeMoveListener(String roomCode);

    void submitActionCardPlay(String roomCode, String playerId, String cardName, Coordinate target, String metadata, DataCallback<Void> callback);
    void addActionCardListener(String roomCode, DataCallback<ActionCardSnapshot> callback);
    void removeActionCardListener(String roomCode);

    void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback);
    void addTurnListener(String roomCode, DataCallback<String> callback);
    void removeTurnListener(String roomCode);

    void updateGameStatus(String roomCode, String status, DataCallback<Void> callback);
    void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback);
    void addPlacementStatusListener(String roomCode, String opponentId, DataCallback<Boolean> callback);

    void updateBoardLayout(String roomCode, String playerId, java.util.List<ShipPlacement> ships, DataCallback<Void> callback);
    void addBoardLayoutListener(String roomCode, String opponentId, DataCallback<java.util.List<ShipPlacement>> callback);
    void removeBoardLayoutListener(String roomCode);

    void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback);

    void sendHeartbeat(String roomCode, String playerId);
    void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback);
    void removeHeartbeatListener(String roomCode);

    void sendPreview(String roomCode, String playerId, Coordinate target);
    void clearPreview(String roomCode, String playerId);
    void addPreviewListener(String roomCode, String opponentId, DataCallback<Coordinate> callback);
    void removePreviewListener(String roomCode);

    void roomIsActive(String roomCode, DataCallback<Boolean> callback);
    void loadGameState(String roomCode, DataCallback<GameSnapshot> callback);
    void cleanupSession(String roomCode, DataCallback<Void> callback);
    void removeAllListeners(String roomCode);

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

    class ActionCardSnapshot {
        public final String playerId;
        public final String cardName;
        public final Coordinate target;
        public final String metadata;
        public final long timestamp;

        public ActionCardSnapshot(String playerId, String cardName, Coordinate target, String metadata, long timestamp) {
            this.playerId = playerId;
            this.cardName = cardName;
            this.target = target;
            this.metadata = metadata;
            this.timestamp = timestamp;
        }
    }

    class GameSnapshot {
        public final String currentTurn;
        public final String status;

        public GameSnapshot(String currentTurn, String status) {
            this.currentTurn = currentTurn;
            this.status      = status;
        }
    }
}
