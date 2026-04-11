package battleships_ex.gdx.state;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public interface GameState {

    String getName();

    void onEnter(GameStateManager manager);
    void onExit(GameStateManager manager);

    // Lobby
    void onCreateLobby(GameStateManager manager);
    void onJoinLobby(GameStateManager manager, String roomCode);
    void onLobbyReady(GameStateManager manager);

    // Placement — Ship, Coordinate, Orientation matches GameController.placeShip exactly
    void onPlaceShip(GameStateManager manager,
                     Ship ship, Coordinate start, Orientation orientation);

    void onRemoveShip(GameStateManager manager, Coordinate coordinate);

    void onPlacementComplete(GameStateManager manager);

    // Shots — row/col matches GameController.fireShot(int row, int col)
    void onFireShot(GameStateManager manager, int row, int col);
    void onRemoteShotReceived(GameStateManager manager, int row, int col);

    // Terminal
    void onGameOver(GameStateManager manager, String winnerName);
}
