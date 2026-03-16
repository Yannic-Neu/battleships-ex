package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.lobby.Lobby;

/**
 * LobbyListener
 *
 * Callback interface the View implements to receive lobby events from
 * {@link LobbyController}.
 */
public interface LobbyListener {

    /**
     * A new lobby was successfully created on the backend.
     * View should display the room code prominently so the host can share it.
     *
     * @param lobby    the created lobby domain object
     * @param roomCode the 6-character room code to display
     */
    void onLobbyCreated(Lobby lobby, String roomCode);

    /**
     * The local player successfully joined an existing lobby as guest.
     * View should show a "waiting for host to start" state.
     *
     * @param lobby the lobby that was joined
     */
    void onLobbyJoined(Lobby lobby);

    /**
     * The guest player joined this lobby (host-side notification).
     * View should update to show both players and enable the ready button.
     *
     * @param guest the player who just joined
     */
    void onGuestJoined(Player guest);

    /**
     * Both players are present and the host has confirmed ready.
     * View should transition to the ship placement screen.
     */
    void onLobbyReady();

    /**
     * A join attempt was rejected. View should display inline feedback
     * without leaving the lobby screen.
     *
     * @param reason structured reason — View switches on this, never parses strings
     */
    void onJoinRejected(LobbyController.JoinRejectionReason reason);

    /**
     * The opponent disconnected before the game started.
     * View should revert to a "waiting for opponent" state.
     */
    void onOpponentDisconnected();
}
