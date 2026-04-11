package battleships_ex.gdx.state;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

/**
 * PlacementState — local player is placing ships on their board.
 *
 * Valid actions: onPlaceShip, onPlacementComplete
 * All shot and lobby actions are silently blocked (inherited no-ops).
 */
public class PlacementState extends BaseGameState {

    private boolean localReady = false;
    private boolean remoteReady = false;

    @Override
    public String getName() { return "PlacementState"; }

    @Override
    public void onEnter(GameStateManager manager) {
        // Initialize the GameSession now that both players are known
        String roomCode = null;
        boolean isHost = true;

        if (manager.getLobbyController().getActiveLobby() != null) {
            roomCode = manager.getLobbyController().getActiveLobby().getRoomCode();
            isHost = manager.getLobbyController().isLocalPlayerHost();
        }

        manager.getGameController().initSession(
            manager.getLocalPlayer(),
            manager.getRemotePlayer(),
            roomCode,
            isHost
        );

        manager.notifyStateChanged(getName());

        // Listen for opponent readiness
        manager.getGameController().addPlacementStatusListener(ready -> {
            remoteReady = ready;
            checkBothReady(manager);
        });
    }

    /**
     * Forwards to {@link battleships_ex.gdx.controller.GameController#placeShip}.
     * Validates via RulesEngine; notifies View via GameListener on success or rejection.
     */
    @Override
    public void onPlaceShip(GameStateManager manager,
                            Ship ship, Coordinate start, Orientation orientation) {
        manager.getGameController().placeShip(ship, start, orientation);
    }

    @Override
    public void onRemoveShip(GameStateManager manager, Coordinate coordinate) {
        manager.getGameController().removeShipAt(coordinate);
    }

    /**
     * All ships placed — wait for both players to be ready.
     */
    @Override
    public void onPlacementComplete(GameStateManager manager) {
        localReady = true;
        manager.getGameController().confirmPlacement();
        checkBothReady(manager);
    }

    private void checkBothReady(GameStateManager manager) {
        if (localReady && remoteReady) {
            manager.getGameController().startGame();

            if (manager.getGameController().isLocalPlayerTurn()) {
                manager.transitionTo(new MyTurnState());
            } else {
                manager.transitionTo(new OpponentTurnState());
            }
        }
    }
}
