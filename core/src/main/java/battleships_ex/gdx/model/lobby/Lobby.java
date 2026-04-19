package battleships_ex.gdx.model.lobby;

import battleships_ex.gdx.model.core.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a game lobby where players gather before a match starts.
 * A lobby requires exactly two players to be ready.
 */
public class Lobby {

    public static final int MAX_PLAYERS = 2;

    private final long id;
    private final String roomCode;
    private final List<Player> players;
    private final List<String> selectedCardNames = new ArrayList<>();

    public Lobby(long id, String roomCode) {
        this.id = id;
        this.roomCode = roomCode;
        this.players = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setSelectedCards(List<String> cards) {
        this.selectedCardNames.clear();
        if (cards != null) {
            this.selectedCardNames.addAll(cards);
        }
    }

    public List<String> getSelectedCards() {
        return Collections.unmodifiableList(selectedCardNames);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Adds a player to the lobby.
     *
     * @param player the player to add
     * @throws IllegalStateException if the lobby is already full
     */
    public void addPlayer(Player player) {
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Lobby is full");
        }
        players.add(player);
    }

    /**
     * @return true if the lobby has enough players to start a game
     */
    public boolean isReady() {
        return players.size() == MAX_PLAYERS;
    }

    /**
     * @return the host player (first player who joined), or null if empty
     */
    public Player getHost() {
        return players.isEmpty() ? null : players.get(0);
    }

    /**
     * @return the guest player (second player), or null if not yet joined
     */
    public Player getGuest() {
        return players.size() < 2 ? null : players.get(1);
    }
}
