package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.GameSession;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;
import battleships_ex.gdx.model.rules.ShotResult;
import battleships_ex.gdx.model.rules.StandardRulesEngine;

/**
 * GameController
 *
 * The single coordinator between the View layer and the domain model.
 * Owns the active {@link GameSession}, delegates all rule evaluation to
 * {@link StandardRulesEngine}, and reports outcomes back to the View via
 * {@link GameListener} callbacks.
 **/
public class GameController {

    private final StandardRulesEngine engine;
    private final FirebaseClient       firebase;

    private GameSession  session;    // null until startGame() is called
    private Player       localPlayer;
    private Player       remotePlayer;
    private GameListener listener;   // null until View registers itself

    /**
     * @param engine   the shared, stateless rules engine
     * @param firebase thin wrapper around Firebase Realtime Database writes
     */
    public GameController(StandardRulesEngine engine, FirebaseClient firebase) {
        this.engine   = engine;
        this.firebase = firebase;
    }

    /**
     * Registers the View to receive game event callbacks.
     * Call this when the game screen becomes active; pass null when leaving
     * the screen to avoid holding a stale reference.
     */
    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    // Session lifecycle ------------------------------------------------------

    /**
     * Initialises a new game session for two players.
     * Must be called before any ship placements or shots.
     *
     * @param local  the local (this device's) player
     * @param remote the opponent player
     */
    public void initSession(Player local, Player remote) {
        this.localPlayer  = local;
        this.remotePlayer = remote;
        this.session      = new GameSession(local, remote);
    }

    /**
     * Starts the game after both players have finished placing their ships.
     * Calls GameSession#startGame() which sets the first active player.
     *
     * @throws IllegalStateException if initSession() has not been called
     */
    public void startGame() {
        requireSession();
        session.startGame();
    }


    // Ship placement phase--------------------------------------------------

    /**
     * Attempts to place a ship on the local player's board.
     *
     * Delegates validation to the rules engine before mutating any board state.
     * If valid, calls Board#placeShip and notifies the listener. If invalid,
     * notifies the listener with the rejection reason so the View can show
     * specific feedback without a screen transition.
     *
     * @param size       number of cells the ship occupies
     * @param startX     column of the anchor cell
     * @param startY     row of the anchor cell
     * @param horizontal true for left-to-right; false for top-to-bottom
     */
    public void placeShip(int size, int startX, int startY, boolean horizontal) {
        requireSession();

        PlacementResult result = engine.validatePlacement(
            localPlayer.getBoard(), size, startX, startY, horizontal);

        if (!result.isValid()) {
            notify_placementRejected(result.getReason());
            return;
        }

        // Validation passed — commit to board
        localPlayer.getBoard().placeShip(startX, startY, size, horizontal);

        // Retrieve the Ship the Board just registered so we can pass it to the listener.
        // Board always appends to its internal list, so the last entry is the one just added.
        Ship placed = lastShipOn(localPlayer);
        notify_shipPlaced(placed);
    }


    // Shot phase — local player fires --------------------------------------------

    /**
     * Processes a shot fired by the local player at the given coordinate.
     *
     * Guards:
     *   - Session must be started.
     *   - It must currently be the local player's turn.
     *   - The game must not already be over.
     *
     * On success:
     *   1. Resolves the shot via the rules engine.
     *   2. Records the move in the session's history.
     *   3. Notifies the View via the listener callback.
     *   4. If the game is now over, notifies the View and returns early.
     *   5. Pushes a compact shot event to Firebase (coordinate only — not
     *      the full board state, per agents.md §4 payload management).
     *   6. Switches turn if the shot was a miss (rules engine already
     *      handled the board mutation; session handles turn logic).
     *
     * @param x column index of the target cell
     * @param y row index of the target cell
     */
    public void fireShot(int x, int y) {
        if (!isSessionActive())  return;
        if (!isLocalPlayerTurn()) return;

        Coordinate target = new Coordinate(x, y);
        ShotResult result  = engine.resolveShot(remotePlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case ALREADY_SHOT:
                notify_alreadyShot(target);
                return;  // no state change — don't record a move or push to Firebase

            case MISS:
                session.processMove(target);   // records move + switches turn
                notify_miss(target);
                firebase.pushShotEvent(target, false);
                break;

            case HIT:
                session.processMove(target);   // records move; turn stays (hit-again)
                notify_hit(target, result.getSunkShip());
                firebase.pushShotEvent(target, true);
                break;

            case SUNK:
                session.processMove(target);
                notify_sunk(target, result.getSunkShip());
                firebase.pushShotEvent(target, true);

                // Check win condition after every sunk ship
                if (engine.hasWon(remotePlayer.getBoard())) {
                    notify_gameOver(localPlayer.getName());
                    firebase.pushGameOver(localPlayer.getName());
                }
                break;
        }
    }

    // Shot phase — remote player fires --------------------------------------------

    /**
     * Processes a shot the remote player fired at the local player's board.
     *
     * This method is called from a Firebase ValueEventListener callback,
     * which arrives on a background thread. The caller (Firebase integration
     * layer) is responsible for marshalling this call onto the GL thread via
     * {@code Gdx.app.postRunnable()} before invoking it.
     *
     * The method resolves the shot against the LOCAL player's board, updates
     * session state, and notifies the View so it can update the local defence
     * grid display.
     *
     * @param x column the remote player targeted
     * @param y row the remote player targeted
     */
    public void onRemoteShotReceived(int x, int y) {
        if (!isSessionActive()) return;

        Coordinate target = new Coordinate(x, y);
        ShotResult result  = engine.resolveShot(localPlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case MISS:
                // Remote player missed → it becomes local player's turn.
                // session.processMove switches turn on a miss automatically.
                session.processMove(target);
                // No listener callback needed for a miss on the defence grid;
                // the View only needs to mark the cell. Add onDefenceMiss() to
                // GameListener if the View requires explicit notification.
                break;

            case HIT:
            case SUNK:
                session.processMove(target);
                if (result.isSunk() && engine.hasWon(localPlayer.getBoard())) {
                    notify_gameOver(remotePlayer.getName());
                }
                break;

            case ALREADY_SHOT:
                // Firebase sent a duplicate event — ignore silently.
                break;
        }
    }


    // Convenience queries for the View ----------------------------------------

    /**
     * @return true if the game session is active and it is the local player's turn
     */
    public boolean isLocalPlayerTurn() {
        return session != null
            && session.isStarted()
            && !session.gameIsOver()
            && session.getCurrentPlayer() == localPlayer;
    }

    /**
     * @return true if a session exists, has been started, and is not yet over
     */
    public boolean isSessionActive() {
        return session != null && session.isStarted() && !session.gameIsOver();
    }

    /**
     * @return the current {@link GameSession}, or null if not yet initialised
     */
    public GameSession getSession() {
        return session;
    }

    // helpers — listener notification------------------------------------------

    // Null-check wrappers so every call site doesn't need its own guard.

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

    // -------------------------------------------------------------------------
    // Private helpers — misc
    // -------------------------------------------------------------------------

    private void requireSession() {
        if (session == null) {
            throw new IllegalStateException(
                "GameController: initSession() must be called before this operation.");
        }
    }

    /**
     * Returns the most recently registered Ship on the player's board.
     * Safe to call immediately after Board#placeShip because Board appends
     * to its internal list and getShips() returns an unmodifiable view of it.
     */
    private Ship lastShipOn(Player player) {
        java.util.List<Ship> ships = player.getBoard().getShips();
        return ships.get(ships.size() - 1);
    }

    // -------------------------------------------------------------------------
    // FirebaseClient — inner interface #TODO (not sure)
    // -------------------------------------------------------------------------

    /**
     * Thin abstraction over Firebase Realtime Database writes.
     *
     * Keeping this as an inner interface means:
     *   - GameController has no direct Firebase SDK import.
     *   - The real implementation lives in the networking layer (agents.md §4).
     *   - Tests can inject a no-op stub without any Firebase dependency.
     *
     * Payload contract (agents.md §4):
     *   - pushShotEvent sends coordinate + hit flag only (not board state).
     *   - pushGameOver sends the winner's name only.
     *   - Full state snapshots are handled separately on reconnect; not here.
     */
    public interface FirebaseClient {

        /**
         * Pushes a compact shot event to Firebase.
         *
         * @param coordinate the cell that was targeted
         * @param hit        true if a ship was struck, false for a miss
         */
        void pushShotEvent(Coordinate coordinate, boolean hit);

        /**
         * Notifies Firebase that the game has ended.
         *
         * @param winnerName display name of the winning player
         */
        void pushGameOver(String winnerName);
    }
}
