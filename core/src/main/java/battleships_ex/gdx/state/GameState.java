package battleships_ex.gdx.state;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * GameState
 * Phase → valid actions:
 *
 *   LobbyState          createLobby, joinLobby
 *   PlacementState      placeShip
 *   MyTurnState         fireShot
 *   OpponentTurnState   (all input actions blocked)
 *   GameOverState       (all input actions blocked)
 */
public interface GameState {
    String getName();
    void onEnter(GameStateManager manager);
    void onExit(GameStateManager manager);
    void onCreateLobby(GameStateManager manager);
    void onJoinLobby(GameStateManager manager, String roomCode);
    void onLobbyReady(GameStateManager manager);
    void onPlaceShip(GameStateManager manager, int size, int startX, int startY, boolean horizontal);
    void onPlacementComplete(GameStateManager manager);
    void onFireShot(GameStateManager manager, int x, int y);
    void onRemoteShotReceived(GameStateManager manager, int x, int y);
    void onGameOver(GameStateManager manager, String winnerName);
}
