package battleships_ex.gdx.model.lobby;

import battleships_ex.gdx.model.core.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    @Test
    void newLobbyIsNotReady() {
        Lobby lobby = new Lobby(1, "ABC123");
        assertFalse(lobby.isReady());
        assertTrue(lobby.getPlayers().isEmpty());
    }

    @Test
    void lobbyIsReadyWithTwoPlayers() {
        Lobby lobby = new Lobby(1, "ABC123");
        lobby.addPlayer(new Player(1, "Alice"));
        assertFalse(lobby.isReady());

        lobby.addPlayer(new Player(2, "Bob"));
        assertTrue(lobby.isReady());
    }

    @Test
    void addPlayerToFullLobbyThrows() {
        Lobby lobby = new Lobby(1, "ABC123");
        lobby.addPlayer(new Player(1, "Alice"));
        lobby.addPlayer(new Player(2, "Bob"));

        assertThrows(IllegalStateException.class,
            () -> lobby.addPlayer(new Player(3, "Charlie")));
    }

    @Test
    void hostAndGuestReturnsCorrectPlayers() {
        Lobby lobby = new Lobby(1, "ABC123");
        assertNull(lobby.getHost());
        assertNull(lobby.getGuest());

        Player host = new Player(1, "Alice");
        lobby.addPlayer(host);
        assertEquals(host, lobby.getHost());
        assertNull(lobby.getGuest());

        Player guest = new Player(2, "Bob");
        lobby.addPlayer(guest);
        assertEquals(guest, lobby.getGuest());
    }

    @Test
    void roomCodeIsPreserved() {
        Lobby lobby = new Lobby(42, "XY7Z9K");
        assertEquals(42, lobby.getId());
        assertEquals("XY7Z9K", lobby.getRoomCode());
    }
}
