package battleships_ex.gdx.data;

/**
 * Interface for lobby-related backend operations.
 * Abstracts the data source so Firebase can be swapped for another backend
 * without affecting controllers or UI (see M3: Swapping Backend Provider).
 */
public interface LobbyDataSource {

    /**
     * Creates a new lobby on the backend with the given room code.
     *
     * @param roomCode unique room code for the lobby
     * @param hostPlayerId unique identifier of the hosting player
     * @param callback notified on success (null) or failure
     */
    void createLobby(String roomCode, String hostPlayerId, DataCallback<Void> callback);

    /**
     * Joins an existing lobby identified by the room code.
     *
     * @param roomCode the room code to join
     * @param playerId unique identifier of the joining player
     * @param callback notified with success or failure (e.g. room not found, room full)
     */
    void joinLobby(String roomCode, String playerId, DataCallback<Void> callback);

    /**
     * Removes a player from a lobby.
     *
     * @param roomCode the room code of the lobby
     * @param playerId the player to remove
     * @param callback notified on success or failure
     */
    void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback);

    /**
     * Checks whether a lobby with the given room code exists and is joinable.
     *
     * @param roomCode the room code to check
     * @param callback notified with true if joinable, false otherwise
     */
    void lobbyExists(String roomCode, DataCallback<Boolean> callback);

    /**
     * Registers a listener for real-time lobby state changes (e.g. player joined).
     * Implementations should push updates whenever the lobby data changes on the backend.
     *
     * @param roomCode the room code to listen to
     * @param callback notified with updated lobby status on each change
     */
    void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback);

    /**
     * Removes a previously registered lobby listener.
     *
     * @param roomCode the room code to stop listening to
     */
    void removeLobbyListener(String roomCode);

    /**
     * Lightweight snapshot of lobby state received from the backend.
     * Keeps the interface independent of model classes.
     */
    class LobbySnapshot {
        public final String roomCode;
        public final String hostPlayerId;
        public final String guestPlayerId;
        public final String status; // "waiting", "ready", "playing"

        public LobbySnapshot(String roomCode, String hostPlayerId, String guestPlayerId, String status) {
            this.roomCode = roomCode;
            this.hostPlayerId = hostPlayerId;
            this.guestPlayerId = guestPlayerId;
            this.status = status;
        }

        public boolean isFull() {
            return guestPlayerId != null;
        }
    }
}
