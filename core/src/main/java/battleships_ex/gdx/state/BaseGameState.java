package battleships_ex.gdx.state;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

/**
 * BaseGameState
 *
 * Default no-op implementations of every {@link GameState} method.
 * Concrete states extend this and override only the actions valid in their phase.
 * Unhandled calls are silently dropped — stale Firebase events or UI mis-fires
 * cannot corrupt state.
 *
 * onGameOver defaults to transitioning to GameOverState so every phase
 * handles disconnects and game-end signals without repeating the logic.
 */
public abstract class BaseGameState implements GameState {

    @Override public void onEnter(GameStateManager manager) { }
    @Override public void onExit(GameStateManager manager)  { }

    @Override public void onCreateLobby(GameStateManager manager) { }
    @Override public void onJoinLobby(GameStateManager manager, String roomCode) { }
    @Override public void onLobbyReady(GameStateManager manager) { }

    @Override
    public void onPlaceShip(GameStateManager manager,
                            Ship ship, Coordinate start, Orientation orientation) { }

    @Override public void onPlacementComplete(GameStateManager manager) { }

    @Override public void onFireShot(GameStateManager manager, int row, int col) { }
    @Override public void onRemoteShotReceived(GameStateManager manager, int row, int col) { }

    @Override
    public void onGameOver(GameStateManager manager, String winnerName) {
        manager.transitionTo(new GameOverState(winnerName));
    }
}
