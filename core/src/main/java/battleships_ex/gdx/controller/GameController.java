package battleships_ex.gdx.controller;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.SessionManager;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.GameSession;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;
import battleships_ex.gdx.model.rules.ShotResult;
import battleships_ex.gdx.model.rules.StandardRulesEngine;
import battleships_ex.gdx.model.core.SimpleBot;

/**
 * Orchestrates game flow: ship placement, shot resolution, action cards,
 * and real-time synchronization via {@link GameDataSource}.
 *
 * <p>Replaces the former inner {@code FirebaseClient} interface with the
 * platform-agnostic {@link GameDataSource} abstraction (Issue #27).</p>
 */
public class GameController {

    private final StandardRulesEngine engine;
    private final GameDataSource      gameDataSource;
    private final SessionManager      sessionManager;

    /** Minimum interval between preview updates (debounce). */
    private static final long PREVIEW_DEBOUNCE_MS = 200L;

    private GameSession  session;
    private Player       localPlayer;
    private Player       remotePlayer;
    private GameListener listener;
    private String       roomCode;

    /** Timestamp of the last preview update sent. */
    private long lastPreviewSentMs;
    /** The last preview coordinate sent (to avoid duplicate sends). */
    private Coordinate lastPreviewCoord;
    private boolean isSinglePlayer = false;
    private SimpleBot botLogic;

    public GameController(StandardRulesEngine engine,
                          GameDataSource gameDataSource,
                          SessionManager sessionManager) {
        this.engine         = engine;
        this.gameDataSource = gameDataSource;
        this.sessionManager = sessionManager;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    /**
     * Initialises the game session and wires up real-time listeners.
     *
     * @param local    the local player
     * @param remote   the remote player
     * @param roomCode the active room code for Firebase sync
     */
    public void initSession(Player local, Player remote, String roomCode) {
        this.localPlayer  = local;
        this.remotePlayer = remote;
        this.roomCode     = roomCode;
        this.session      = new GameSession(local, remote);
    }

    /**
     * Backward-compatible overload for local/stub usage without room code.
     */
    public void initSession(Player local, Player remote) {
        initSession(local, remote, null);
    }

    /**
     * Starts the game and activates real-time synchronization.
     */
    public void startGame() {
        requireSession();
        session.startGame();

        if (roomCode != null) {
            // Update game status to "playing"
            gameDataSource.updateGameStatus(roomCode, "playing", new DataCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Failed to update game status: " + error);
                }
            });

            // Sync initial turn
            gameDataSource.syncTurn(roomCode, session.getCurrentPlayer().getId(), new DataCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Failed to sync initial turn: " + error);
                }
            });

            // Register move listener for opponent moves
            gameDataSource.addMoveListener(roomCode, new DataCallback<GameDataSource.MoveSnapshot>() {
                @Override
                public void onSuccess(GameDataSource.MoveSnapshot move) {
                    // Only process moves from the opponent
                    if (!move.playerId.equals(localPlayer.getId())) {
                        onRemoteShotReceived(move.row, move.col);
                        if (listener != null) {
                            listener.onOpponentMoveReceived(move.row, move.col);
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Move listener error: " + error);
                }
            });

            // Start session manager (heartbeat + disconnect detection)
            sessionManager.setListener(new SessionManager.SessionListener() {
                @Override
                public void onOpponentDisconnected() {
                    if (listener != null) listener.onOpponentDisconnected();
                }

                @Override
                public void onOpponentReconnected() {
                    if (listener != null) listener.onOpponentReconnected();
                }

                @Override
                public void onSessionTimeout() {
                    if (listener != null) listener.onSessionTimeout();
                }
            });

            sessionManager.startSession(roomCode, localPlayer.getId(), remotePlayer.getId());

            // Register preview listener to see opponent's aim (Issue #28)
            gameDataSource.addPreviewListener(roomCode, remotePlayer.getId(), new DataCallback<Coordinate>() {
                @Override
                public void onSuccess(Coordinate previewCoord) {
                    if (listener != null) {
                        listener.onPreviewReceived(previewCoord);
                    }
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Preview listener error: " + error);
                }
            });
        }
    }

    /**
     * Sends a target preview to the opponent (Issue #28).
     * The preview is debounced to avoid flooding Firebase with updates.
     *
     * @param target the coordinate being aimed at
     */
    public void updatePreview(Coordinate target) {
        if (!isSessionActive() || !isLocalPlayerTurn()) return;
        if (roomCode == null) return;

        long now = System.currentTimeMillis();

        // Skip if same coordinate or within debounce window
        if (target.equals(lastPreviewCoord) && (now - lastPreviewSentMs) < PREVIEW_DEBOUNCE_MS) {
            return;
        }

        lastPreviewCoord = target;
        lastPreviewSentMs = now;
        gameDataSource.sendPreview(roomCode, localPlayer.getId(), target);
    }

    /**
     * Clears the target preview after a shot is confirmed.
     */
    private void clearPreview() {
        if (roomCode == null) return;
        lastPreviewCoord = null;
        gameDataSource.clearPreview(roomCode, localPlayer.getId());
    }

    public void placeShip(Ship ship, Coordinate start, Orientation orientation) {
        requireSession();

        PlacementResult result = engine.validatePlacement(
            localPlayer.getBoard(), ship, start, orientation);

        if (!result.isValid()) {
            notify_placementRejected(result.getReason());
            return;
        }

        localPlayer.getBoard().placeShip(ship, start, orientation);

        notify_shipPlaced(ship);
    }

    public void playActionCard(battleships_ex.gdx.model.cards.ActionCard card) {
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) return;

        battleships_ex.gdx.model.cards.ActionCardResult result = session.playActionCard(card);

        if (listener != null) listener.onActionCardPlayed(result);

        // Reset inactivity timer on action
        sessionManager.resetInactivityTimer();

        // Check win condition - some cards (e.g. Airstrike) can sink ships
        if (result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.SUNK
            && engine.hasWon(remotePlayer.getBoard())) {
            notify_gameOver(localPlayer.getName());
            if (roomCode != null) {
                gameDataSource.pushGameOver(roomCode, localPlayer.getName(), new DataCallback<Void>() {
                    @Override public void onSuccess(Void r) {}
                    @Override public void onFailure(String error) {
                        System.out.println("[GameController] Failed to push game over: " + error);
                    }
                });
            }
        }
    }

    public void fireShot(int row, int col) {
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) return;

        Coordinate target = new Coordinate(row, col);
        ShotResult result  = engine.resolveShot(remotePlayer.getBoard(), target);

        // Clear preview now that shot is confirmed (Issue #28)
        clearPreview();

        // Reset inactivity timer on action
        sessionManager.resetInactivityTimer();

        switch (result.getOutcome()) {

            case ALREADY_SHOT:
                notify_alreadyShot(target);
                return;

            case MISS:
                session.processMove(target);    // records move + switches turn
                notify_miss(target);
                syncMoveToBackend(target, false);
                syncTurnToBackend();
                break;

            case HIT:
                session.processMove(target);    // records move; turn stays (hit-again)
                notify_hit(target, result.getSunkShip());
                syncMoveToBackend(target, true);
                break;

            case SUNK:
                session.processMove(target);
                notify_sunk(target, result.getSunkShip());
                syncMoveToBackend(target, true);

                if (engine.hasWon(remotePlayer.getBoard())) {
                    notify_gameOver(localPlayer.getName());
                    if (roomCode != null) {
                        gameDataSource.pushGameOver(roomCode, localPlayer.getName(), new DataCallback<Void>() {
                            @Override public void onSuccess(Void r) {}
                            @Override public void onFailure(String error) {
                                System.out.println("[GameController] Failed to push game over: " + error);
                            }
                        });
                    }
                }
                break;
        }
    }

    public void onRemoteShotReceived(int row, int col) {
        if (!isSessionActive()) return;

        Coordinate target = new Coordinate(row, col);
        ShotResult result  = engine.resolveShot(localPlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case MISS:
                session.processMove(target);    // switches turn back to local
                break;

            case HIT:
            case SUNK:
                session.processMove(target);
                if (result.isSunk() && engine.hasWon(localPlayer.getBoard())) {
                    notify_gameOver(remotePlayer.getName());
                }
                break;

            case ALREADY_SHOT:
                // Stale Firebase event — ignore silently.
                break;
        }
    }

    /**
     * Cleans up all real-time listeners and session state.
     * Call when leaving the battle screen.
     */
    public void cleanup() {
        sessionManager.endSession();
        if (roomCode != null) {
            gameDataSource.removeAllListeners(roomCode);
        }
    }

    public boolean isLocalPlayerTurn() {
        return session != null
            && session.isStarted()
            && !session.gameIsOver()
            && session.getCurrentPlayer() == localPlayer;
    }

    /** @return true if a session is active and not yet over */
    public boolean isSessionActive() {
        return session != null && session.isStarted() && !session.gameIsOver();
    }

    /** @return the current GameSession, or null if not yet initialised */
    public GameSession getSession() {
        return session;
    }

    /** @return the current room code */
    public String getRoomCode() {
        return roomCode;
    }

    // ── Backend sync helpers ────────────────────────────────────────

    private void syncMoveToBackend(Coordinate target, boolean hit) {
        if (roomCode == null) return;

        gameDataSource.submitMove(roomCode, localPlayer.getId(), target, hit, new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String error) {
                System.out.println("[GameController] Failed to sync move: " + error);
            }
        });
    }

    private void syncTurnToBackend() {
        if (roomCode == null) return;

        gameDataSource.syncTurn(roomCode, session.getCurrentPlayer().getId(), new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String error) {
                System.out.println("[GameController] Failed to sync turn: " + error);
            }
        });
    }

    // ── Notification helpers ────────────────────────────────────────

    private void notify_miss(Coordinate c) {
        if (listener != null) listener.onMiss(c);
    }

    private void notify_hit(Coordinate c, Ship ship) {
        if (listener != null) listener.onHit(c, ship);
    }

    private void notify_sunk(Coordinate c, Ship ship) {
        if (listener != null) listener.onSunk(c, ship);
    }

    private void notify_gameOver(String winnerName) {
        if (listener != null) listener.onGameOver(winnerName);
    }

    private void notify_alreadyShot(Coordinate c) {
        if (listener != null) listener.onAlreadyShot(c);
    }

    private void notify_shipPlaced(Ship ship) {
        if (listener != null) listener.onShipPlaced(ship);
    }

    private void notify_placementRejected(PlacementResult.Reason reason) {
        if (listener != null) listener.onPlacementRejected(reason);
    }

    private void requireSession() {
        if (session == null) {
            throw new IllegalStateException(
                "GameController: initSession() must be called before this operation.");
        }
    }
    /**
     * Initialises a local-only session against a simple computer opponent.
     * Reuses the stubbed Data Source natively via initSession.
     */
    public void initSinglePlayerSession(Player local) {
        this.isSinglePlayer = true;

        Player remote = new Player("BOT", "Computer");
        this.botLogic = new SimpleBot();
        this.botLogic.placeHardcodedShips(remote.getBoard());

        debugPrintEnemyBoard(remote.getBoard());

        // Using the overloaded initSession without a room code ensures
        // it functions locally (usually leveraging StubGameDataSource).
        initSession(local, remote);
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    /**
     * Fetches the next predetermined move and feeds it into the standard remote shot pipeline.
     */
    public void playBotTurn() {
        if (botLogic == null || !isSessionActive()) return;

        Coordinate target = botLogic.getNextMove();
        onRemoteShotReceived(target.getRow(), target.getCol());
    }
    private void debugPrintEnemyBoard(battleships_ex.gdx.model.board.Board board) {
        System.out.println("=== ENEMY BOT BOARD ===");
        for (int row = 0; row < board.getHeight(); row++) {
            StringBuilder rowString = new StringBuilder();
            for (int col = 0; col < board.getWidth(); col++) {
                // Check if the cell has a ship using the methods from Board.java
                if (board.getCell(row, col).hasShip()) {
                    rowString.append("[S]"); // S for Ship
                } else {
                    rowString.append("[ ]"); // Empty
                }
            }
            System.out.println(rowString.toString());
        }
        System.out.println("=======================");
    }
}
