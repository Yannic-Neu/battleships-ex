package battleships_ex.gdx.state;

import battleships_ex.gdx.controller.GameController;
import battleships_ex.gdx.controller.GameListener;
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.controller.LobbyListener;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;

public class GameStateManager {

    private static GameStateManager instance;

    /// Returns the singleton instance
    public static GameStateManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "GameStateManager not initialised. Call init() first.");
        }
        return instance;
    }

    public static void init(GameController  gameController,
                            LobbyController lobbyController,
                            Player          localPlayer) {
        instance = new GameStateManager(gameController, lobbyController, localPlayer);
    }

    public static void destroy() {
        instance = null;
    }

    private final GameController  gameController;
    private final LobbyController lobbyController;
    private final Player          localPlayer;

    ///  Mutable
    private GameState         currentState;
    private Player            remotePlayer;
    private GameStateListener stateListener;

    private GameStateManager(GameController  gameController,
                             LobbyController lobbyController,
                             Player          localPlayer) {
        this.gameController  = gameController;
        this.lobbyController = lobbyController;
        this.localPlayer     = localPlayer;

        // Have internal listeners st. state transitions fire automatically
        gameController.setListener(buildGameListener());
        lobbyController.setListener(buildLobbyListener());

        currentState = new LobbyState();
        currentState.onEnter(this);
    }

    public void setStateListener(GameStateListener listener) {
        this.stateListener = listener;
    }

    /// Request to create a new lobby. valid only in LobbyState
    public void createLobby() {
        currentState.onCreateLobby(this);
    }

    public void joinLobby(String roomCode) {
        currentState.onJoinLobby(this, roomCode);
    }

    /// Place a ship during setup. Valid only in PlacementState.
    public void placeShip(int size, int startX, int startY, boolean horizontal) {
        currentState.onPlaceShip(this, size, startX, startY, horizontal);
    }

    public void confirmPlacementComplete() {
        currentState.onPlacementComplete(this);
    }

    public void fireShot(int x, int y) {
        currentState.onFireShot(this, x, y);
    }

    void onRemoteShotReceived(int x, int y) {
        currentState.onRemoteShotReceived(this, x, y);
    }

    public void onOpponentDisconnected() {
        currentState.onGameOver(this, "Opponent disconnected");
    }

    void transitionTo(GameState next) {
        if (next == null) return;
        currentState.onExit(this);
        currentState = next;
        currentState.onEnter(this);
    }

    /// Notifies the View that the active phase has changed
    void notifyStateChanged(String stateName) {
        if (stateListener != null) stateListener.onStateChanged(stateName);
    }
    void notifyGameOver(String winnerName) {
        if (stateListener != null) stateListener.onGameOver(winnerName);
    }

    GameController getGameController() {
        return gameController;
    }
    LobbyController getLobbyController() {
        return lobbyController;
    }

    Player getLocalPlayer() {
        return localPlayer;
    }
    Player getRemotePlayer() {
        return remotePlayer;
    }

    public String getCurrentStateName() {
        return currentState.getName();
    }

    public boolean isMyTurn() {
        return currentState instanceof MyTurnState;
    }

    public boolean isGameOver() {
        return currentState instanceof GameOverState;
    }

    private GameListener buildGameListener() {
        return new GameListener() {

            @Override
            public void onMiss(Coordinate coordinate) {
                // Determine which direction the miss came from and drive the
                // turn-switch transition.
                if (currentState instanceof MyTurnState) {
                    ((MyTurnState) currentState).onShotMissed(GameStateManager.this);
                } else if (currentState instanceof OpponentTurnState) {
                    ((OpponentTurnState) currentState).onRemoteMissed(GameStateManager.this);
                }
                // Forward to View listener
                if (stateListener != null) stateListener.onMiss(coordinate);
            }

            @Override
            public void onHit(Coordinate coordinate, Ship ship) {
                // Hit-again: no transition, stay in current turn state.
                if (stateListener != null) stateListener.onHit(coordinate, ship);
            }

            @Override
            public void onSunk(Coordinate coordinate, Ship ship) {
                // Sunk but game not over: stay in current turn state.
                if (stateListener != null) stateListener.onSunk(coordinate, ship);
            }

            @Override
            public void onGameOver(String winnerName) {
                currentState.onGameOver(GameStateManager.this, winnerName);
                // GameOverState.onEnter calls notifyGameOver
            }

            @Override
            public void onAlreadyShot(Coordinate coordinate) {
                if (stateListener != null) stateListener.onAlreadyShot(coordinate);
            }

            @Override
            public void onShipPlaced(Ship ship) {
                if (stateListener != null) stateListener.onShipPlaced(ship);
            }

            @Override
            public void onPlacementRejected(PlacementResult.Reason reason) {
                if (stateListener != null) stateListener.onPlacementRejected(reason);
            }
        };
    }

    private LobbyListener buildLobbyListener() {
        return new LobbyListener() {

            @Override
            public void onLobbyCreated(battleships_ex.gdx.model.lobby.Lobby lobby, String roomCode) {
                if (stateListener != null) stateListener.onLobbyCreated(roomCode);
            }

            @Override
            public void onLobbyJoined(battleships_ex.gdx.model.lobby.Lobby lobby) {
                if (stateListener != null) stateListener.onLobbyJoined();
            }

            @Override
            public void onGuestJoined(Player guest) {
                if (stateListener != null) stateListener.onGuestJoined(guest.getName());
            }

            @Override
            public void onLobbyReady() {
                // Resolve remote player from the active lobby stored in LobbyController
                battleships_ex.gdx.model.lobby.Lobby lobby = lobbyController.getActiveLobby();
                if (lobby != null) {
                    Player host  = lobby.getHost();
                    Player guest = lobby.getGuest();
                    remotePlayer = host.getId().equals(localPlayer.getId()) ? guest : host;
                }
                currentState.onLobbyReady(GameStateManager.this);
            }

            @Override
            public void onJoinRejected(LobbyController.JoinRejectionReason reason) {
                if (stateListener != null) stateListener.onJoinRejected(reason);
            }

            @Override
            public void onOpponentDisconnected() {
                currentState.onGameOver(GameStateManager.this, "Opponent disconnected");
            }
        };
    }
}
