package battleships_ex.gdx;

import battleships_ex.gdx.data.SessionStore;
import battleships_ex.gdx.data.SessionStore.SessionInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionStore} contract and {@link SessionInfo} expiry logic.
 * Uses an in-memory implementation for unit testing.
 */
class SessionRejoinTest {

    private InMemorySessionStore store;

    @BeforeEach
    void setUp() {
        store = new InMemorySessionStore();
    }

    @Test
    void saveAndRetrieveSession() {
        SessionInfo info = new SessionInfo("ROOM1", "player1", "player2", System.currentTimeMillis());
        store.saveSession(info);

        SessionInfo retrieved = store.getActiveSession();
        assertNotNull(retrieved);
        assertEquals("ROOM1", retrieved.roomCode);
        assertEquals("player1", retrieved.playerId);
        assertEquals("player2", retrieved.opponentId);
    }

    @Test
    void getActiveSession_returnsNullWhenEmpty() {
        assertNull(store.getActiveSession());
    }

    @Test
    void clearSession_removesData() {
        store.saveSession(new SessionInfo("ROOM1", "p1", "p2", System.currentTimeMillis()));
        store.clearSession();

        assertNull(store.getActiveSession());
    }

    @Test
    void sessionInfo_isValid_withinTimeWindow() {
        SessionInfo info = new SessionInfo("R", "p1", "p2", System.currentTimeMillis());
        assertTrue(info.isValid(10 * 60 * 1000L)); // 10 minutes
    }

    @Test
    void sessionInfo_isExpired_outsideTimeWindow() {
        long tenMinutesAgo = System.currentTimeMillis() - (11L * 60L * 1000L);
        SessionInfo info = new SessionInfo("R", "p1", "p2", tenMinutesAgo);
        assertFalse(info.isValid(10 * 60 * 1000L)); // 10 minutes
    }

    @Test
    void getActiveSession_returnsNullForExpiredSession() {
        long twentyMinutesAgo = System.currentTimeMillis() - (20L * 60L * 1000L);
        store.saveSession(new SessionInfo("ROOM1", "p1", "p2", twentyMinutesAgo));

        // The store's max age is 10 minutes, so this should be null
        assertNull(store.getActiveSession());
    }

    @Test
    void saveSession_overwritesPrevious() {
        store.saveSession(new SessionInfo("ROOM1", "p1", "p2", System.currentTimeMillis()));
        store.saveSession(new SessionInfo("ROOM2", "p3", "p4", System.currentTimeMillis()));

        SessionInfo retrieved = store.getActiveSession();
        assertNotNull(retrieved);
        assertEquals("ROOM2", retrieved.roomCode);
        assertEquals("p3", retrieved.playerId);
    }

    // ── In-memory test implementation ───────────────────────────────

    private static class InMemorySessionStore implements SessionStore {
        private static final long MAX_AGE_MS = 10L * 60L * 1000L;
        private SessionInfo stored;

        @Override
        public void saveSession(SessionInfo sessionInfo) {
            this.stored = sessionInfo;
        }

        @Override
        public SessionInfo getActiveSession() {
            if (stored == null) return null;
            if (!stored.isValid(MAX_AGE_MS)) {
                clearSession();
                return null;
            }
            return stored;
        }

        @Override
        public void clearSession() {
            this.stored = null;
        }
    }
}
