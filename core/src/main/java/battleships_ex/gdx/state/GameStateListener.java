package battleships_ex.gdx.state;

import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.rules.PlacementResult;

/**
 * GameStateListener
 *
 * The single callback interface the View implements to receive all events
 * from {@link GameStateManager}.
 */
public interface GameStateListener {

    void onStateChanged(String stateName);
    void onLobbyCreated(String roomCode);
    void onGuestJoined(String guestName);
    void onJoinRejected(LobbyController.JoinRejectionReason reason);
    void onShipPlaced(Ship ship);
    void onPlacementRejected(PlacementResult.Reason reason);
    void onMiss(Coordinate coordinate);
    void onHit(Coordinate coordinate, Ship ship);
    void onSunk(Coordinate coordinate, Ship ship);
    void onAlreadyShot(Coordinate coordinate);
    void onGameOver(String winnerName);
}
