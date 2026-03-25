package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.lobby.Lobby;

public interface LobbyListener {

    void onLobbyCreated(Lobby lobby, String roomCode);

    void onLobbyJoined(Lobby lobby);

    void onGuestJoined(Player guest);

    void onLobbyReady();

    void onJoinRejected(LobbyController.JoinRejectionReason reason);

    void onOpponentDisconnected();
}
