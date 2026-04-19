package battleships_ex.gdx.state;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.controller.GameController;
import battleships_ex.gdx.controller.GameListener;
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.controller.LobbyListener;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;

/**
 * GameStateManager
 *
 * Singleton that owns the active {@link GameState} and drives all phase
 * transitions. Single entry point for all View actions.
 *
 * Architectural contracts (agents.md §2):
 *   - Singleton: one consistent game state shared across all application layers.
 *   - State Pattern: the active state decides whether each action is valid.
 *   - One-way: View → GameStateManager → active state → controller.
 *     Results flow back via {@link GameStateListener}.
 *
 * Threading: all public methods must be called on the libGDX GL thread.
 * Firebase callbacks must be marshalled via Gdx.app.postRunnable() first.
 */
public class GameStateManager {

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static GameStateManager instance;

    /** @throws IllegalStateException if {@link #init} has not been called */
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

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final GameController  gameController;
    private final LobbyController lobbyController;
    private final Player          localPlayer;

    // -------------------------------------------------------------------------
    // Mutable state
    // -------------------------------------------------------------------------

    private GameState         currentState;
    private Player            remotePlayer;
    private GameStateListener stateListener;
    private boolean           exModeEnabled = true;

    // -------------------------------------------------------------------------
    // Construction (private — use init())
    // -------------------------------------------------------------------------

    private GameStateManager(GameController  gameController,
                             LobbyController lobbyController,
                             Player          localPlayer) {
        this.gameController  = gameController;
        this.lobbyController = lobbyController;
        this.localPlayer     = localPlayer;

        gameController.setListener(buildGameListener());
        lobbyController.setListener(buildLobbyListener());

        currentState = new LobbyState();
        currentState.onEnter(this);
    }

    // -------------------------------------------------------------------------
    // View registration
    // -------------------------------------------------------------------------

    public void setStateListener(GameStateListener listener) {
        this.stateListener = listener;
    }

    // -------------------------------------------------------------------------
    // View-facing action API
    // -------------------------------------------------------------------------

    /** Bypasses the LobbyState entirely for offline/bot matches. */
    public void forceSinglePlayerPlacement(Player botPlayer) {
        this.remotePlayer = botPlayer;
        transitionTo(new PlacementState());
    }

    /** Forces transition to PlacementState for multiplayer after manual UI join. */
    public void forceMultiplayerPlacement(Player remotePlayer) {
        this.remotePlayer = remotePlayer;
        transitionTo(new PlacementState());
    }

    /** Request to create a new lobby. Valid only in LobbyState. */
    public void createLobby() {
        currentState.onCreateLobby(this);
    }

    /** Request to join a lobby by room code. Valid only in LobbyState. */
    public void joinLobby(String roomCode) {
        currentState.onJoinLobby(this, roomCode);
    }

    /**
     * Place a ship during setup. Valid only in PlacementState.
     *
     * @param ship        the ship to place (ShipType + Orientation already set)
     * @param start       anchor coordinate (top / left cell)
     * @param orientation HORIZONTAL or VERTICAL
     */
    public void placeShip(Ship ship, Coordinate start, Orientation orientation) {
        currentState.onPlaceShip(this, ship, start, orientation);
    }

    public void removeShip(Coordinate coordinate) {
        currentState.onRemoveShip(this, coordinate);
    }

    /** Signal that all ships are placed. Valid only in PlacementState. */
    public void confirmPlacementComplete() {
        currentState.onPlacementComplete(this);
    }

    /**
     * Fire a shot. Valid only in MyTurnState.
     *
     * @param row target row on the opponent's board
     * @param col target column on the opponent's board
     */
    public void fireShot(int row, int col) {
        currentState.onFireShot(this, row, col);
    }

    /**
     * Remote shot received from Firebase. Valid only in OpponentTurnState.
     * Must be called on the GL thread via Gdx.app.postRunnable().
     */
    public void onRemoteShotReceived(int row, int col) {
        currentState.onRemoteShotReceived(this, row, col);
    }

    /** Opponent disconnected — transitions to GameOverState regardless of phase. */
    public void onOpponentDisconnected() {
        currentState.onGameOver(this, "Opponent disconnected");
    }

    // -------------------------------------------------------------------------
    // State transition (package-private — only states call this)
    // -------------------------------------------------------------------------

    void transitionTo(GameState next) {
        if (next == null) return;
        System.out.println("[GameStateManager] LOG: Transitioning from " + currentState.getName() + " to " + next.getName());
        currentState.onExit(this);
        currentState = next;
        currentState.onEnter(this);
    }

    // -------------------------------------------------------------------------
    // Notification helpers (package-private — states call these)
    // -------------------------------------------------------------------------

    void notifyStateChanged(String stateName) {
        if (stateListener != null) stateListener.onStateChanged(stateName);
    }

    void notifyGameOver(String winnerName) {
        if (stateListener != null) stateListener.onGameOver(winnerName);
    }

    // -------------------------------------------------------------------------
    // Accessors (package-private — states use these)
    // -------------------------------------------------------------------------

    GameController  getGameController()  { return gameController; }
    LobbyController getLobbyController() { return lobbyController; }
    Player          getLocalPlayer()     { return localPlayer; }
    Player          getRemotePlayer()    { return remotePlayer; }

    // -------------------------------------------------------------------------
    // Public accessors for the View
    // -------------------------------------------------------------------------

    public String  getCurrentStateName() { return currentState.getName(); }
    public boolean isMyTurn()            { return currentState instanceof MyTurnState; }
    public boolean isGameOver()          { return currentState instanceof GameOverState; }
    public boolean isExModeEnabled()     { return exModeEnabled; }
    public void    setExModeEnabled(boolean enabled) { this.exModeEnabled = enabled; }

    // -------------------------------------------------------------------------
    // Internal listener wiring
    // -------------------------------------------------------------------------

    private GameListener buildGameListener() {
        return new GameListener() {

            @Override
            public void onMiss(Coordinate coordinate) {
                if (currentState instanceof MyTurnState) {
                    ((MyTurnState) currentState).onShotMissed(GameStateManager.this);
                } else if (currentState instanceof OpponentTurnState) {
                    ((OpponentTurnState) currentState).onRemoteMissed(GameStateManager.this);
                }
                if (stateListener != null) stateListener.onMiss(coordinate);
            }

            @Override
            public void onHit(Coordinate coordinate, Ship ship) {
                if (stateListener != null) stateListener.onHit(coordinate, ship);
            }

            @Override
            public void onSunk(Coordinate coordinate, Ship ship) {
                if (stateListener != null) stateListener.onSunk(coordinate, ship);
            }

            @Override
            public void onGameOver(String winnerName) {
                currentState.onGameOver(GameStateManager.this, winnerName);
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
            public void onShipRemoved(Ship ship) {
                if (stateListener != null) stateListener.onShipRemoved(ship);
            }

            @Override
            public void onPlacementRejected(PlacementResult.Reason reason) {
                if (stateListener != null) stateListener.onPlacementRejected(reason);
            }

            @Override
            public void onOpponentPlacementReady(boolean ready) {
                if (stateListener != null) {
                    stateListener.onOpponentPlacementReady(ready);
                }
            }
@Override
public void onActionCardPlayed(battleships_ex.gdx.model.cards.ActionCardResult result) {
    if (stateListener != null) {
        stateListener.onActionCardPlayed(result);
    }
}

@Override
public void onActionCardRejected(battleships_ex.gdx.model.cards.ActionCard card, String reason) {
    if (stateListener != null) {
        stateListener.onActionCardRejected(card, reason);
    }
}

@Override
public void onCardTargetRequested(battleships_ex.gdx.model.cards.ActionCard card) {

    if (stateListener != null) {
        stateListener.onCardTargetRequested(card);
    }
}

            @Override
            public void onTurnChanged(String currentPlayerId) {
                System.out.println("[GameStateManager] LOG: onTurnChanged received. currentPlayerId: " + currentPlayerId + ", localPlayerId: " + localPlayer.getId());
                if (stateListener != null) stateListener.onTurnChanged(currentPlayerId);

                if (currentPlayerId.equals(localPlayer.getId())) {
                    if (!(currentState instanceof MyTurnState)) {
                        transitionTo(new MyTurnState());
                    }
                } else {
                    if (!(currentState instanceof OpponentTurnState)) {
                        transitionTo(new OpponentTurnState());
                    }
                }
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
