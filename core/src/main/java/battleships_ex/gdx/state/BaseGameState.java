package battleships_ex.gdx.state;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * BaseGameState
 *
 * Provides default no-op implementations of every {@link GameState} method.
 */
public abstract class BaseGameState implements GameState {

    @Override
    public void onEnter(GameStateManager manager) { }

    @Override
    public void onExit(GameStateManager manager) { }

    @Override
    public void onCreateLobby(GameStateManager manager) { }

    @Override
    public void onJoinLobby(GameStateManager manager, String roomCode) { }

    @Override
    public void onLobbyReady(GameStateManager manager) { }

    @Override
    public void onPlaceShip(GameStateManager manager, int size, int startX, int startY, boolean horizontal) { }

    @Override
    public void onPlacementComplete(GameStateManager manager) { }

    @Override
    public void onFireShot(GameStateManager manager, int x, int y) { }

    @Override
    public void onRemoteShotReceived(GameStateManager manager, int x, int y) { }

    @Override
    public void onGameOver(GameStateManager manager, String winnerName) {
        // Every state can receive a game-over signal (e.g. opponent disconnects
        // mid-placement). Default implementation performs the transition so
        // concrete states don't need to repeat it.
        manager.transitionTo(new GameOverState(winnerName));
    }
}
