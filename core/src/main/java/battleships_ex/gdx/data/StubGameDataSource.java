package battleships_ex.gdx.data;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * No-op stub for desktop testing where Firebase is unavailable.
 * Logs calls to stdout for development purposes.
 * Follows the same pattern as {@link StubLobbyDataSource}.
 */
public class StubGameDataSource implements GameDataSource {

    @Override
    public void submitMove(String roomCode, String playerId, Coordinate target, boolean hit, DataCallback<Void> callback) {
        System.out.println("[Stub] submitMove: room=" + roomCode
            + " player=" + playerId
            + " target=(" + target.getRow() + "," + target.getCol() + ")"
            + " hit=" + hit);
        callback.onSuccess(null);
    }

    @Override
    public void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback) {
        System.out.println("[Stub] addMoveListener: " + roomCode);
    }

    @Override
    public void removeMoveListener(String roomCode) {
        System.out.println("[Stub] removeMoveListener: " + roomCode);
    }

    @Override
    public void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback) {
        System.out.println("[Stub] syncTurn: room=" + roomCode + " turn=" + currentPlayerId);
        callback.onSuccess(null);
    }

    @Override
    public void addTurnListener(String roomCode, DataCallback<String> callback) {
        System.out.println("[Stub] addTurnListener: " + roomCode);
    }

    @Override
    public void removeTurnListener(String roomCode) {
        System.out.println("[Stub] removeTurnListener: " + roomCode);
    }

    @Override
    public void updateGameStatus(String roomCode, String status, DataCallback<Void> callback) {
        System.out.println("[Stub] updateGameStatus: room=" + roomCode + " status=" + status);
        callback.onSuccess(null);
    }

    @Override
    public void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback) {
        System.out.println("[Stub] updatePlacementStatus: room=" + roomCode + " player=" + playerId + " ready=" + isReady);
        callback.onSuccess(null);
    }

    @Override
    public void addPlacementStatusListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
        System.out.println("[Stub] addPlacementStatusListener: room=" + roomCode + " opponent=" + opponentId);
        // In stub mode, assume opponent is ready immediately for easy testing
        callback.onSuccess(true);
    }

    @Override
    public void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback) {
        System.out.println("[Stub] pushGameOver: room=" + roomCode + " winner=" + winnerName);
        callback.onSuccess(null);
    }

    @Override
    public void sendHeartbeat(String roomCode, String playerId) {
        System.out.println("[Stub] sendHeartbeat: room=" + roomCode + " player=" + playerId);
    }

    @Override
    public void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
        System.out.println("[Stub] addHeartbeatListener: room=" + roomCode + " opponent=" + opponentId);
    }

    @Override
    public void removeHeartbeatListener(String roomCode) {
        System.out.println("[Stub] removeHeartbeatListener: " + roomCode);
    }

    @Override
    public void sendPreview(String roomCode, String playerId, Coordinate target) {
        System.out.println("[Stub] sendPreview: room=" + roomCode
            + " player=" + playerId
            + " target=(" + target.getRow() + "," + target.getCol() + ")");
    }

    @Override
    public void clearPreview(String roomCode, String playerId) {
        System.out.println("[Stub] clearPreview: room=" + roomCode + " player=" + playerId);
    }

    @Override
    public void addPreviewListener(String roomCode, String opponentId, DataCallback<Coordinate> callback) {
        System.out.println("[Stub] addPreviewListener: room=" + roomCode + " opponent=" + opponentId);
    }

    @Override
    public void removePreviewListener(String roomCode) {
        System.out.println("[Stub] removePreviewListener: " + roomCode);
    }

    @Override
    public void roomIsActive(String roomCode, DataCallback<Boolean> callback) {
        System.out.println("[Stub] roomIsActive: " + roomCode);
        callback.onSuccess(false);
    }

    @Override
    public void loadGameState(String roomCode, DataCallback<GameSnapshot> callback) {
        System.out.println("[Stub] loadGameState: " + roomCode);
        callback.onSuccess(new GameSnapshot(null, "finished"));
    }

    @Override
    public void cleanupSession(String roomCode, DataCallback<Void> callback) {
        System.out.println("[Stub] cleanupSession: " + roomCode);
        callback.onSuccess(null);
    }

    @Override
    public void removeAllListeners(String roomCode) {
        System.out.println("[Stub] removeAllListeners: " + roomCode);
    }
}
