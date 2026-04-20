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
    private SessionStore sessionStore;
    private SessionListener listener;

    private String roomCode;
    private String localPlayerId;
    private String opponentPlayerId;
    private boolean active;
    private long lastOpponentHeartbeat = 0;

    private Timer.Task heartbeatTask;
    private Timer.Task inactivityTask;
    private Timer.Task monitorTask;

    public SessionManager(GameDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Sets the session store for persisting session data across restarts.
     * Must be called before {@link #startSession} for rejoin to work.
     */
    public void setSessionStore(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
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

        // Persist session for potential rejoin (Issue #29)
        if (sessionStore != null) {
            sessionStore.saveSession(new SessionStore.SessionInfo(
                roomCode, localPlayerId, opponentPlayerId, System.currentTimeMillis()));
        }

        if (isGdxAvailable()) {
            startHeartbeat();
            resetInactivityTimer();
            startStalenessMonitor();
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
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        if (roomCode != null) {
            dataSource.removeHeartbeatListener(roomCode);
        }

        // Clear persisted session (Issue #29)
        if (sessionStore != null) {
            sessionStore.clearSession();
        }
    }

    // ── Session re-join (Issue #29) ─────────────────────────────────

    /**
     * Checks whether a persisted session exists that can be rejoined.
     *
     * @return true if there is a valid (non-expired) persisted session
     */
    public boolean hasActiveSession() {
        if (sessionStore == null) return false;
        return sessionStore.getActiveSession() != null;
    }

    /**
     * Returns the persisted session info, or null if none exists.
     */
    public SessionStore.SessionInfo getPersistedSession() {
        if (sessionStore == null) return null;
        return sessionStore.getActiveSession();
    }

    /**
     * Attempts to rejoin a persisted session by verifying the room
     * is still active on the backend.
     *
     * @param callback delivers true if rejoin is possible, false otherwise
     */
    public void tryRejoin(DataCallback<Boolean> callback) {
        SessionStore.SessionInfo info = getPersistedSession();
        if (info == null) {
            callback.onSuccess(false);
            return;
        }

        dataSource.roomIsActive(info.roomCode, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isActive) {
                if (isActive) {
                    callback.onSuccess(true);
                } else {
                    // Room is gone — clean up stale session
                    sessionStore.clearSession();
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(String error) {
                System.out.println("[SessionManager] rejoin check failed: " + error);
                callback.onSuccess(false);
            }
        });
    }

    /**
     * Cleans up an abandoned session on the backend.
     *
     * @param roomCode the room to clean up
     */
    public void cleanupAbandonedSession(String roomCode) {
        dataSource.cleanupSession(roomCode, new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {
                System.out.println("[SessionManager] Cleaned up abandoned room: " + roomCode);
            }

            @Override public void onFailure(String error) {
                System.out.println("[SessionManager] Cleanup failed: " + error);
            }
        });
        if (sessionStore != null) {
            sessionStore.clearSession();
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

    private void startStalenessMonitor() {
        // Check every 3 seconds if the opponent's heartbeat is stale
        monitorTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!active || listener == null || lastOpponentHeartbeat == 0) return;

                // HEARTBEAT_STALE_MS is 15s in FirebaseGameDataSource, 
                // we'll use a slightly safer margin here.
                long elapsed = System.currentTimeMillis() - lastOpponentHeartbeat;
                if (elapsed > 20000L) { // 20 seconds threshold
                    System.out.println("[SessionManager] Monitor detected stale heartbeat (" + elapsed + "ms)");
                    listener.onOpponentDisconnected();
                }
            }
        }, 3f, 3f);
    }

    private void startOpponentMonitoring() {
        if (roomCode == null || opponentPlayerId == null) return;

        dataSource.addHeartbeatListener(roomCode, opponentPlayerId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean disconnected) {
                if (!active) return;
                
                lastOpponentHeartbeat = System.currentTimeMillis();
                
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
