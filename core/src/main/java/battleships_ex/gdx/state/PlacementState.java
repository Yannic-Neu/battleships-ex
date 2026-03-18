package battleships_ex.gdx.state;

public class PlacementState extends BaseGameState {

    @Override
    public String getName() {
        return "PlacementState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        // Initialise the GameSession now that both players are known
        manager.getGameController().initSession(
            manager.getLocalPlayer(),
            manager.getRemotePlayer());
        manager.notifyStateChanged(getName());
    }

    @Override
    public void onPlaceShip(battleships_ex.gdx.state.GameStateManager manager, int size, int startX, int startY, boolean horizontal) {
        manager.getGameController().placeShip(size, startX, startY, horizontal);
    }

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
