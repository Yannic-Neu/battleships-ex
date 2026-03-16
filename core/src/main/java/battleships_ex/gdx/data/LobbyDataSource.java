package battleships_ex.gdx.data;

/**
 * Interface for lobby-related backend operations.
 * Abstracts the data source so Firebase can be swapped for another backend
 * without affecting controllers or UI (see M3: Swapping Backend Provider).
 */
public interface LobbyDataSource {
    void createLobby(String roomCode, String hostPlayerId, String hostPlayerName, DataCallback<Void> callback);
    void joinLobby(String roomCode, String playerId, String playerName, DataCallback<Void> callback);
    void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback);
    void lobbyExists(String roomCode, DataCallback<Boolean> callback);
    void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback);
    void removeLobbyListener(String roomCode);
    class LobbySnapshot {
        public final String roomCode;
        public final String hostPlayerId;
        public final String hostPlayerName;
        public final String guestPlayerId;    // null if guest slot is empty
        public final String guestPlayerName;  // null if guest slot is empty
        public final String status;           // "waiting", "ready", "playing"

        public LobbySnapshot(String roomCode,
                             String hostPlayerId,  String hostPlayerName,
                             String guestPlayerId, String guestPlayerName,
                             String status) {
            this.roomCode        = roomCode;
            this.hostPlayerId    = hostPlayerId;
            this.hostPlayerName  = hostPlayerName;
            this.guestPlayerId   = guestPlayerId;
            this.guestPlayerName = guestPlayerName;
            this.status          = status;
        }

        /** @return true if the guest slot is filled */
        public boolean isFull() {
            return guestPlayerId != null;
        }
    }
}
