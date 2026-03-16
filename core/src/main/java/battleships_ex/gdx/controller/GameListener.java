package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

/**
 * GameListener
 *
 * Callback interface the View (or any screen) implements to receive game
 * events from {@link GameController}.
 *
 */
public interface GameListener {

    /**
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
}
