package battleships_ex.gdx.data;

/**
 * No-op stub for desktop testing where Firebase is unavailable.
 * Logs calls to stdout for development purposes.
 */
public class StubLobbyDataSource implements LobbyDataSource {

    @Override
    public void createLobby(String roomCode,
                            String hostPlayerId,
                            String hostPlayerName,
                            DataCallback<Void> callback) {
        System.out.println("[Stub] createLobby: " + roomCode
            + " host=" + hostPlayerId + " (" + hostPlayerName + ")");
        callback.onSuccess(null);
    }

    @Override
    public void joinLobby(String roomCode,
                          String playerId,
                          String playerName,
                          DataCallback<Void> callback) {
        System.out.println("[Stub] joinLobby: " + roomCode
            + " player=" + playerId + " (" + playerName + ")");
        callback.onSuccess(null);
    }

    @Override
    public void leaveLobby(String roomCode,
                           String playerId,
                           DataCallback<Void> callback) {
        System.out.println("[Stub] leaveLobby: " + roomCode + " player=" + playerId);
        callback.onSuccess(null);
    }

    @Override
    public void lobbyExists(String roomCode, DataCallback<Boolean> callback) {
        System.out.println("[Stub] lobbyExists: " + roomCode);
        callback.onSuccess(false);
    }

    @Override
    public void addLobbyListener(String roomCode,
                                 DataCallback<LobbySnapshot> callback) {
        System.out.println("[Stub] addLobbyListener: " + roomCode);
    }

    @Override
    public void removeLobbyListener(String roomCode) {
        System.out.println("[Stub] removeLobbyListener: " + roomCode);
    }

    @Override
    public void setGuestReady(String roomCode, boolean ready, DataCallback<Void> callback) {
        System.out.println("[Stub] setGuestReady: " + roomCode + " ready=" + ready);
        callback.onSuccess(null);
    }

    @Override
    public void setLobbyStatus(String roomCode, String status, DataCallback<Void> callback) {
        System.out.println("[Stub] setLobbyStatus: " + roomCode + " status=" + status);
        callback.onSuccess(null);
    }

    @Override
    public void setExMode(String roomCode, boolean enabled, DataCallback<Void> callback) {
        System.out.println("[Stub] setExMode: " + roomCode + " enabled=" + enabled);
        callback.onSuccess(null);
    }
}
