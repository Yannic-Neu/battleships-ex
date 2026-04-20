package battleships_ex.gdx.data;

/**
 * Interface for lobby-related backend operations.
 * Abstracts the data source so Firebase can be swapped for another backend
 * without affecting controllers or UI (see M3: Swapping Backend Provider).
 */
public interface LobbyDataSource {

    void createLobby(String roomCode, String hostPlayerId, String hostPlayerName, DataCallback<Void> callback);

    default void createLobby(String roomCode, String hostPlayerId, DataCallback<Void> callback) {
        createLobby(roomCode, hostPlayerId, "", callback);
    }

    void joinLobby(String roomCode, String playerId, String playerName, DataCallback<Void> callback);

    default void joinLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        joinLobby(roomCode, playerId, "", callback);
    }

    void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback);

    void lobbyExists(String roomCode, DataCallback<Boolean> callback);

    void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback);

    void removeLobbyListener(String roomCode);

    void setGuestReady(String roomCode, boolean ready, DataCallback<Void> callback);

    void setLobbyStatus(String roomCode, String status, DataCallback<Void> callback);

    void setExMode(String roomCode, boolean enabled, DataCallback<Void> callback);

    void setSelectedCards(String roomCode, java.util.List<String> cardNames, DataCallback<Void> callback);

    class LobbySnapshot {
        public final String roomCode;
        public final String hostPlayerId;
        public final String hostPlayerName;
        public final String guestPlayerId;    // null if guest slot is empty
        public final String guestPlayerName;  // null if guest slot is empty
        public final String status;           // "waiting", "joined", "ready", "playing"
        public final boolean guestReady;
        public final boolean exModeEnabled;
        public final java.util.List<String> selectedCardNames;

        public LobbySnapshot(String roomCode,
                             String hostPlayerId,  String hostPlayerName,
                             String guestPlayerId, String guestPlayerName,
                             String status, boolean guestReady,
                             boolean exModeEnabled,
                             java.util.List<String> selectedCardNames) {
            this.roomCode        = roomCode;
            this.hostPlayerId    = hostPlayerId;
            this.hostPlayerName  = hostPlayerName;
            this.guestPlayerId   = guestPlayerId;
            this.guestPlayerName = guestPlayerName;
            this.status          = status;
            this.guestReady      = guestReady;
            this.exModeEnabled   = exModeEnabled;
            this.selectedCardNames = selectedCardNames != null ? 
                java.util.Collections.unmodifiableList(selectedCardNames) : 
                java.util.Collections.emptyList();
        }

        public LobbySnapshot(String roomCode,
                             String hostPlayerId,  String hostPlayerName,
                             String guestPlayerId, String guestPlayerName,
                             String status, boolean guestReady) {
            this(roomCode, hostPlayerId, hostPlayerName, guestPlayerId,
                 guestPlayerName, status, guestReady, true, java.util.Collections.emptyList());
        }

        /** @return true if the guest slot is filled */
        public boolean isFull() {
            return guestPlayerId != null;
        }
    }
}
