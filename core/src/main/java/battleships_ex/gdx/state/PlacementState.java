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

    @Override
    public String getName() { return "PlacementState"; }

    @Override
    public void onEnter(GameStateManager manager) {
        // Initialise the GameSession now that both players are known
        manager.getGameController().initSession(
            manager.getLocalPlayer(),
            manager.getRemotePlayer());
        manager.notifyStateChanged(getName());
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
     * All ships placed — start the game and transition based on who goes first.
     */
    @Override
    public void onPlacementComplete(GameStateManager manager) {
        manager.getGameController().startGame();

        if (manager.getGameController().isLocalPlayerTurn()) {
            manager.transitionTo(new MyTurnState());
        } else {
            manager.transitionTo(new OpponentTurnState());
        }
    }
}
