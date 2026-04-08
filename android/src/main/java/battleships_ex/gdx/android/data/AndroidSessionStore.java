package battleships_ex.gdx.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import battleships_ex.gdx.data.SessionStore;

/**
 * Android implementation of {@link SessionStore} using SharedPreferences.
 * Persists session data so the player can rejoin after app restart.
 */
public class AndroidSessionStore implements SessionStore {

    private static final String PREFS_NAME       = "battleships_session";
    private static final String KEY_ROOM_CODE    = "roomCode";
    private static final String KEY_PLAYER_ID    = "playerId";
    private static final String KEY_OPPONENT_ID  = "opponentId";
    private static final String KEY_SAVED_AT     = "savedAt";

    /** Sessions older than 10 minutes are considered expired. */
    private static final long MAX_AGE_MS = 10L * 60L * 1000L;

    private final SharedPreferences prefs;

    public AndroidSessionStore(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveSession(SessionInfo sessionInfo) {
        prefs.edit()
            .putString(KEY_ROOM_CODE,   sessionInfo.roomCode)
            .putString(KEY_PLAYER_ID,   sessionInfo.playerId)
            .putString(KEY_OPPONENT_ID, sessionInfo.opponentId)
            .putLong(KEY_SAVED_AT,      sessionInfo.savedAt)
            .apply();
    }

    @Override
    public SessionInfo getActiveSession() {
        String roomCode   = prefs.getString(KEY_ROOM_CODE, null);
        String playerId   = prefs.getString(KEY_PLAYER_ID, null);
        String opponentId = prefs.getString(KEY_OPPONENT_ID, null);
        long savedAt      = prefs.getLong(KEY_SAVED_AT, 0L);

        if (roomCode == null || playerId == null) {
            return null;
        }

        SessionInfo info = new SessionInfo(roomCode, playerId, opponentId, savedAt);

        if (!info.isValid(MAX_AGE_MS)) {
            clearSession();
            return null;
        }

        return info;
    }

    @Override
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
