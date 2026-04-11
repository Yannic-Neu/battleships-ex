package battleships_ex.gdx.state;

import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.cards.ActionCardResult;
import battleships_ex.gdx.model.rules.PlacementResult;

/**
 * GameStateListener
 *
 * The single callback interface the View implements to receive all events
 * from {@link GameStateManager}.
 */
public interface GameStateListener {

    // Phase transitions
    void onStateChanged(String stateName);

    // Lobby
    void onLobbyCreated(String roomCode);
    void onLobbyJoined();
    void onGuestJoined(String guestName);
    void onJoinRejected(LobbyController.JoinRejectionReason reason);

    // Placement
    void onShipPlaced(Ship ship);
    void onShipRemoved(Ship ship);
    void onPlacementRejected(PlacementResult.Reason reason);

    // Shots
    void onMiss(Coordinate coordinate);
    void onHit(Coordinate coordinate, Ship ship);
    void onSunk(Coordinate coordinate, Ship ship);
    void onAlreadyShot(Coordinate coordinate);

    // Action cards
    void onActionCardPlayed(ActionCardResult result);

    // Terminal
    void onGameOver(String winnerName);
}
