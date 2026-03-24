package battleships_ex.gdx.data;

/**
 * No-op stub for desktop testing where Firebase is unavailable.
 * Logs calls to stdout for development purposes.
 */
public class StubLobbyDataSource implements LobbyDataSource {

    @Override
    public void createLobby(String roomCode, String hostPlayerId, DataCallback<Void> callback) {
        System.out.println("[Stub] createLobby: " + roomCode);
        callback.onSuccess(null);
    }

    @Override
    public void joinLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        System.out.println("[Stub] joinLobby: " + roomCode);
        callback.onSuccess(null);
    }

    @Override
    public void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        System.out.println("[Stub] leaveLobby: " + roomCode);
        callback.onSuccess(null);
    }

    @Override
    public void lobbyExists(String roomCode, DataCallback<Boolean> callback) {
        System.out.println("[Stub] lobbyExists: " + roomCode);
        callback.onSuccess(true);
    }

    @Override
    public void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback) {
        System.out.println("[Stub] addLobbyListener: " + roomCode);
    }

    @Override
    public void removeLobbyListener(String roomCode) {
        System.out.println("[Stub] removeLobbyListener: " + roomCode);
    }
}
