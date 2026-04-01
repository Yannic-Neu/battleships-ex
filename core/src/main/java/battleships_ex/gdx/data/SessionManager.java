package battleships_ex.gdx.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;

/**
 * Manages game session lifecycle: heartbeat emission, opponent disconnect
 * detection, and inactivity timeout.
 *
 * <p>Usage:
 * <pre>
 *   SessionManager sm = new SessionManager(gameDataSource);
 *   sm.setListener(myListener);
 *   sm.startSession("ABC123", "player1", "player2");
 *   // ... game runs ...
 *   sm.endSession();
 * </pre>
 */
public class SessionManager {

    /** Heartbeat is sent every 5 seconds. */
    private static final float HEARTBEAT_INTERVAL_SEC = 5f;

    /** Session times out after 10 minutes of inactivity. */
    private static final float INACTIVITY_TIMEOUT_SEC = 10f * 60f;

    private final GameDataSource dataSource;
    private SessionListener listener;

    private String roomCode;
    private String localPlayerId;
    private String opponentPlayerId;
    private boolean active;

    private Timer.Task heartbeatTask;
    private Timer.Task inactivityTask;

    public SessionManager(GameDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    /**
     * Starts heartbeat emission and opponent monitoring.
     *
     * @param roomCode         the active room code
     * @param localPlayerId    the local player's id
     * @param opponentPlayerId the opponent's player id
     */
    public void startSession(String roomCode, String localPlayerId, String opponentPlayerId) {
        this.roomCode         = roomCode;
        this.localPlayerId    = localPlayerId;
        this.opponentPlayerId = opponentPlayerId;
        this.active           = true;

        if (isGdxAvailable()) {
            startHeartbeat();
            resetInactivityTimer();
        }
        startOpponentMonitoring();
    }

    /**
     * Stops all timers and removes all listeners.
     * Call when leaving a game session.
     */
    public void endSession() {
        this.active = false;

        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
        if (inactivityTask != null) {
            inactivityTask.cancel();
            inactivityTask = null;
        }
        if (roomCode != null) {
            dataSource.removeHeartbeatListener(roomCode);
        }
    }

    /**
     * Resets the inactivity timeout.
     * Should be called on every user action (shot fired, card played, etc.).
     */
    public void resetInactivityTimer() {
        if (!active) return;
        if (!isGdxAvailable()) return;

        if (inactivityTask != null) {
            inactivityTask.cancel();
        }

        inactivityTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (active && listener != null) {
                    listener.onSessionTimeout();
                }
            }
        }, INACTIVITY_TIMEOUT_SEC);
    }

    /** @return true if a session is currently active */
    public boolean isSessionActive() {
        return active;
    }

    /** @return the current room code, or null if no session */
    public String getRoomCode() {
        return roomCode;
    }

    /** @return the local player's id */
    public String getLocalPlayerId() {
        return localPlayerId;
    }

    /** @return the opponent's player id */
    public String getOpponentPlayerId() {
        return opponentPlayerId;
    }

    // ── Private helpers ─────────────────────────────────────────────

    private boolean isGdxAvailable() {
        try {
            return Gdx.app != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void startHeartbeat() {
        heartbeatTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (active && roomCode != null && localPlayerId != null) {
                    dataSource.sendHeartbeat(roomCode, localPlayerId);
                }
            }
        }, 0f, HEARTBEAT_INTERVAL_SEC);
    }

    private void startOpponentMonitoring() {
        if (roomCode == null || opponentPlayerId == null) return;

        dataSource.addHeartbeatListener(roomCode, opponentPlayerId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean disconnected) {
                if (!active) return;
                if (listener == null) return;

                if (disconnected) {
                    listener.onOpponentDisconnected();
                } else {
                    listener.onOpponentReconnected();
                }
            }

            @Override
            public void onFailure(String error) {
                System.out.println("[SessionManager] heartbeat listener error: " + error);
            }
        });
    }

    // ── Listener interface ──────────────────────────────────────────

    /**
     * Callback interface for session health events.
     */
    public interface SessionListener {

        /** Opponent's heartbeat has gone stale (disconnected). */
        void onOpponentDisconnected();

        /** Opponent's heartbeat has resumed (reconnected). */
        void onOpponentReconnected();

        /** No user activity for the configured timeout period. */
        void onSessionTimeout();
    }
}
