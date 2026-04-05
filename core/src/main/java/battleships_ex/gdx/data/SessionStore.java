package battleships_ex.gdx.data;

/**
 * Interface for persisting active game session info across app restarts.
 * Allows the player to rejoin a game after disconnect.
 *
 * Platform-specific implementations:
 * - Android: SharedPreferences
 * - Desktop: File-based storage
 */
public interface SessionStore {

    /**
     * Saves the current game session so it can be restored later.
     *
     * @param sessionInfo the session details to persist
     */
    void saveSession(SessionInfo sessionInfo);

    /**
     * Retrieves the active session, if one exists and hasn't expired.
     *
     * @return the persisted session info, or null if none exists or it has expired
     */
    SessionInfo getActiveSession();

    /**
     * Clears the persisted session (e.g. after game ends or manual abandon).
     */
    void clearSession();

    /**
     * Holds the essential data needed to rejoin a game session.
     */
    class SessionInfo {
        public final String roomCode;
        public final String playerId;
        public final String opponentId;
        public final long savedAt;

        public SessionInfo(String roomCode, String playerId, String opponentId, long savedAt) {
            this.roomCode   = roomCode;
            this.playerId   = playerId;
            this.opponentId = opponentId;
            this.savedAt    = savedAt;
        }

        /**
         * @param maxAgeMs maximum allowed age in milliseconds
         * @return true if this session info is still within the valid time window
         */
        public boolean isValid(long maxAgeMs) {
            return (System.currentTimeMillis() - savedAt) < maxAgeMs;
        }
    }
}
