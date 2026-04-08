package battleships_ex.gdx.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Desktop implementation of {@link SessionStore} using file-based persistence.
 * Stores session data in a simple text file in the local data directory.
 */
public class FileSessionStore implements SessionStore {

    private static final String SESSION_FILE = "session.dat";
    private static final String SEPARATOR = "|";

    /** Sessions older than 10 minutes are considered expired. */
    private static final long MAX_AGE_MS = 10L * 60L * 1000L;

    @Override
    public void saveSession(SessionInfo sessionInfo) {
        try {
            FileHandle file = Gdx.files.local(SESSION_FILE);
            String data = sessionInfo.roomCode + SEPARATOR
                + sessionInfo.playerId + SEPARATOR
                + sessionInfo.opponentId + SEPARATOR
                + sessionInfo.savedAt;
            file.writeString(data, false);
        } catch (Exception e) {
            System.out.println("[FileSessionStore] Failed to save session: " + e.getMessage());
        }
    }

    @Override
    public SessionInfo getActiveSession() {
        try {
            FileHandle file = Gdx.files.local(SESSION_FILE);
            if (!file.exists()) return null;

            String data = file.readString().trim();
            String[] parts = data.split("\\" + SEPARATOR);
            if (parts.length < 4) return null;

            SessionInfo info = new SessionInfo(
                parts[0],                    // roomCode
                parts[1],                    // playerId
                parts[2],                    // opponentId
                Long.parseLong(parts[3])     // savedAt
            );

            if (!info.isValid(MAX_AGE_MS)) {
                clearSession();
                return null;
            }

            return info;
        } catch (Exception e) {
            System.out.println("[FileSessionStore] Failed to read session: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void clearSession() {
        try {
            FileHandle file = Gdx.files.local(SESSION_FILE);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.out.println("[FileSessionStore] Failed to clear session: " + e.getMessage());
        }
    }
}
