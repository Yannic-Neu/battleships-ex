package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Cell;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

/**
 * API alignment (verified against model/board sources):
 *   - Board:  getCell(int,int), getCell(Coordinate), getSize(), getShips(),
 *             receiveAttack(Coordinate), allShipsSunk()
 *             NOTE: receiveAttack() throws IllegalStateException on duplicate
 *             shots — this engine guards via Cell#isHit() before delegating.
 *   - Cell:   hasShip(), isHit(), getPosition()
 *   - Ship:   isSunk(), getSize(), getPosition() → List<Cell>
 *   - Orientation: not yet implemented — placement uses boolean horizontal.
 *             The interface method accepts Object orientation as a bridge;
 *             swap to Orientation enum once that class exists.
 *
 * Performance contract (P3): every public method completes in ≤ 50 ms.
 * All loops are O(ship-count) or O(ship-size), never O(board²).
 */
public class StandardRulesEngine implements RulesEngine {


    /**
     * Core placement validator; works directly with primitive board coordinates.
     *
     * Checks (short-circuit on first failure):
     *   1. Every cell in the ship footprint lies within the grid boundary.
     *   2. No cell in the footprint overlaps an existing ship.
     *   3. No cell in the 1-cell adjacency buffer around the footprint is
     *      occupied (standard Battleships no-touching rule).
     *
     * @param board      the placing player's own board (not the opponent's)
     * @param size       ship length in cells
     * @param startX     column of the anchor (leftmost / topmost) cell
     * @param startY     row of the anchor cell
     * @param horizontal true → left-to-right; false → top-to-bottom
     * @return a {@link PlacementResult} describing success or the exact failure
     */
    public PlacementResult validatePlacement(Board board,
                                             int size,
                                             int startX,
                                             int startY,
                                             boolean horizontal) {
        int boardSize = board.getSize();

        // ---- 1. Boundary check ------------------------------------------
        int endX = horizontal ? startX + size - 1 : startX;
        int endY = horizontal ? startY             : startY + size - 1;

        if (startX < 0 || startY < 0 || endX >= boardSize || endY >= boardSize) {
            return PlacementResult.failure(PlacementResult.Reason.OUT_OF_BOUNDS);
        }

        // ---- 2 & 3. Overlap + adjacency check ---------------------------
        // Expand the bounding box by 1 in every direction for the spacing buffer.
        int bufX0 = Math.max(0,             startX - 1);
        int bufY0 = Math.max(0,             startY - 1);
        int bufX1 = Math.min(boardSize - 1, endX   + 1);
        int bufY1 = Math.min(boardSize - 1, endY   + 1);

        for (int x = bufX0; x <= bufX1; x++) {
            for (int y = bufY0; y <= bufY1; y++) {
                if (board.getCell(x, y).hasShip()) {
                    boolean inFootprint = (x >= startX && x <= endX
                        && y >= startY && y <= endY);
                    return inFootprint
                        ? PlacementResult.failure(PlacementResult.Reason.OVERLAPS_SHIP)
                        : PlacementResult.failure(PlacementResult.Reason.TOO_CLOSE_TO_SHIP);
                }
            }
        }

        return PlacementResult.success();
    }

    /**
     * Resolves a shot fired at {@code target} on {@code board}.
     *
     * Steps:
     *   1. Read the cell — if already hit, return ALREADY_SHOT immediately.
     *      This avoids the {@link IllegalStateException} that Board#receiveAttack
     *      throws on duplicate shots; the guard belongs here in the rules layer.
     *   2. Delegate to Board#receiveAttack to mark the cell and get hit/miss.
     *   3. On a hit, locate the owning Ship via findShipAt() and check isSunk().
     *
     * @param board  the board being attacked (the opponent's board)
     * @param target the coordinate to fire at
     * @return a {@link ShotResult} carrying the outcome and, when SUNK,
     *         a reference to the sunk {@link Ship}
     */
    @Override
    public ShotResult resolveShot(Board board, Coordinate target) {
        Cell cell = board.getCell(target);

        // ---- Guard: duplicate shot (Board throws on this; we short-circuit) --
        if (cell.isHit()) {
            return ShotResult.alreadyShot(target);
        }

        // ---- Delegate to Board to record the attack ----------------------
        boolean hit = board.receiveAttack(target);  // sets cell.isHit = true

        if (!hit) {
            return ShotResult.miss(target);
        }

        // ---- Locate the owning ship and check sunk status ----------------
        // O(ship-count × ship-size) — bounded to ~17 ships × 5 cells = 85 ops
        Ship hitShip = findShipAt(board, target);

        if (hitShip == null) {
            // Defensive: cell.hasShip() was true but no registered ship contains
            // this coordinate. Should never occur with a correctly populated board.
            // Treat as miss to avoid a null-pointer crash downstream.
            return ShotResult.miss(target);
        }

        return hitShip.isSunk()
            ? ShotResult.sunk(target, hitShip)
            : ShotResult.hit(target, hitShip);
    }


    // ----Win condition --------------------------------------------------
    /**
     * Returns {@code true} when every ship on {@code board} has been sunk.
     *
     * Delegates to {@link Board#allShipsSunk()}, which already returns
     * {@code false} for an empty board — so no ships placed is never a win.
     *
     * @param board the board to evaluate (typically the opponent's)
     */
    @Override
    public boolean hasWon(Board board) {
        return board.allShipsSunk();
    }

    // helpers -----------------------------------------------------------
    /**
     * Scans the ship registry to find which {@link Ship} occupies {@code target}.
     *
     * Returns {@code null} only as a defensive fallback; callers must handle it.
     * Replace with a coordinate-keyed Map<Coordinate, Ship> on Board if this
     * becomes a hot path (e.g., AI rapid-fire scenarios).
     */
    private Ship findShipAt(Board board, Coordinate target) {
        for (Ship ship : board.getShips()) {
            for (Cell cell : ship.getPosition()) {
                if (cell.getPosition().equals(target)) {
                    return ship;
                }
            }
        }
        return null;
    }

    /**
     * Converts the orientation parameter to a boolean until the Orientation
     * enum is introduced.
     *
     * #TODO
     * Migration path: once Orientation is added, replace this with
     *   {@code return orientation == Orientation.HORIZONTAL;}
     * and update the interface signature to use the enum type directly.
     */
    private boolean toHorizontal(Object orientation) {
        if (orientation instanceof Boolean) {
            return (Boolean) orientation;
        }
        return true; // safe default: horizontal
    }
}
