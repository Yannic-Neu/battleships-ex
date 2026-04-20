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

    void onOpponentAbandoned();

    // Phase transitions
    void onStateChanged(String stateName);

    // Lobby
    void onLobbyCreated(String roomCode);
    void onLobbyJoined();
    void onGuestJoined(String guestName);
    void onJoinRejected(LobbyController.JoinRejectionReason reason);

    /** Called when the opponent's placement readiness changes. */
    void onOpponentPlacementReady(boolean ready);

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
    default void onActionCardRejected(battleships_ex.gdx.model.cards.ActionCard card, String reason) {}

    // Terminal
    void onGameOver(String winnerName);

    void onGameOver(String winnerName, String reason);

    void onTurnChanged(String currentPlayerId);

    default void onCardTargetRequested(battleships_ex.gdx.model.cards.ActionCard card) {}
}
