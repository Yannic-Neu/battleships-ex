package battleships_ex.gdx.model.cards;

import java.util.List;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.ShotResult;

/**
 * Defines all possible card effects in a single, extensible interface.
 * Cards describe their intent by calling these methods, while the implementation
 * (usually the RulesEngine) handles the game state transitions.
 */
public interface ActionCardEffect {

    /**
     * Shoots a single tile on the opponent's board.
     */
    ShotResult shootTile(Player opponent, Coordinate coord);

    /**
     * Shoots multiple tiles (e.g., 2x2 grid, row, column).
     */
    List<ShotResult> shootArea(Player opponent, List<Coordinate> coords);

    /**
     * Reveals information about a tile without shooting (e.g., Sonar).
     */
    TileInfo revealTileInfo(Player opponent, Coordinate coord);

    /**
     * Places a mine on the player's own board.
     */
    MinePlacementResult placeMineOnOwnBoard(Player user, Coordinate coord);

    /**
     * Triggers random shots at the opponent's unrevealed tiles.
     */
    List<ShotResult> triggerRandomShots(Player opponent, int count);

    /**
     * Queries tiles matching a specific state (e.g., UNHIT, UNOCCUPIED).
     */
    List<Coordinate> getAvailableTiles(Player opponent, TileState state);

    enum TileInfo {
        EMPTY,
        SHIP,
        MINE
    }

    enum MinePlacementResult {
        SUCCESS,
        INVALID_POSITION,
        ALREADY_HIT
    }

    enum TileState {
        UNHIT,
        UNOCCUPIED,
        EMPTY
    }
}
