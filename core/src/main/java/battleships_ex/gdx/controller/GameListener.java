package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

/**
 * GameListener
 *
 * Callback interface the View (or any screen) implements to receive game
 * events from {@link GameController}.
 *
 * Architectural contract (agents.md §1):
 *   - This is the ONLY way GameController communicates back to the View.
 *   - GameController never imports a concrete View or libGDX Screen class.
 *   - The View registers itself via GameController#setListener() and receives
 *     push notifications here instead of polling game state every frame.
 *
 * Every method is intentionally fine-grained so the View can update only the
 * single affected tile or UI element, never rebuilding the full board
 * (agents.md §3 — avoid full UI rebuilds).
 */
public interface GameListener {

    /**
     * A shot landed on open water.
     * View should mark the cell as a miss and pass the turn indicator to
     * the opponent.
     *
     * @param coordinate the cell that was missed
     */
    void onMiss(Coordinate coordinate);

    /**
     * A shot struck a ship cell, but the ship is still afloat.
     * View should mark the cell as a hit and keep the turn indicator on the
     * current player (hit-again rule).
     *
     * @param coordinate the cell that was hit
     * @param ship       the ship that was struck (not yet sunk)
     */
    void onHit(Coordinate coordinate, Ship ship);

    /**
     * A shot struck the final remaining cell of a ship, sinking it.
     * View should mark the cell as hit, reveal the full ship outline, and
     * display a "sunk" notification.
     *
     * @param coordinate the final hit coordinate
     * @param ship       the ship that was just sunk
     */
    void onSunk(Coordinate coordinate, Ship ship);

    /**
     * The current player has won the game.
     * Called immediately after the final ship is sunk.
     * View should transition to a game-over screen.
     *
     * @param winnerName the display name of the winning player
     */
    void onGameOver(String winnerName);

    /**
     * The player attempted to fire at a cell they already hit.
     * View should show a brief error indicator; no game state has changed.
     *
     * @param coordinate the duplicate coordinate
     */
    void onAlreadyShot(Coordinate coordinate);

    /**
     * A ship was successfully placed on the local board during setup.
     *
     * @param ship the ship that was placed
     */
    void onShipPlaced(Ship ship);

    /**
     * A ship placement was rejected by the rules engine.
     * View should show inline feedback explaining why (e.g. "too close to
     * another ship") without transitioning away from the placement screen.
     *
     * @param reason the specific reason from {@link battleships_ex.gdx.model.rules.PlacementResult.Reason}
     */
    void onPlacementRejected(battleships_ex.gdx.model.rules.PlacementResult.Reason reason);

    /**
     * An action card was played by the local player.
     * View should animate affected cells and show a card-played notification.
     *
     * @param result the outcome and affected coordinates
     */
    void onActionCardPlayed(battleships_ex.gdx.model.cards.ActionCardResult result);

    // ── Real-time session events (Issue #27) ────────────────────────

    /**
     * An opponent move was received from the backend.
     * View should update the local board to reflect the incoming shot.
     *
     * @param row the row of the opponent's shot
     * @param col the column of the opponent's shot
     */
    default void onOpponentMoveReceived(int row, int col) {}

    /**
     * The opponent has disconnected (heartbeat stale).
     * View should display a "waiting for opponent" overlay.
     */
    default void onOpponentDisconnected() {}

    /**
     * The opponent has reconnected after a disconnect.
     * View should dismiss the disconnect overlay.
     */
    default void onOpponentReconnected() {}

    /**
     * The session has timed out due to inactivity.
     * View should navigate back to the menu.
     */
    default void onSessionTimeout() {}

    // ── Target preview events (Issue #28) ───────────────────────────

    /**
     * The opponent is aiming at a cell on our board (preview).
     * View should render a semi-transparent marker on the "Your Fleet" board.
     *
     * @param coordinate the cell the opponent is targeting, or null if cleared
     */
    default void onPreviewReceived(battleships_ex.gdx.model.board.Coordinate coordinate) {}
}
