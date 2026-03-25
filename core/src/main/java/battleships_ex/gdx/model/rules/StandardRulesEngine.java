package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public class StandardRulesEngine implements RulesEngine {

    @Override
    public PlacementResult validatePlacement(Board board,
                                             Ship ship,
                                             Coordinate start,
                                             Orientation orientation) {
        if (ship == null || start == null || orientation == null) {
            return PlacementResult.failure(PlacementResult.Reason.OUT_OF_BOUNDS);
        }

        // ---- Boundary check (fast, no cell reads) -----------------------
        int length = ship.getLength();
        int endRow = orientation == Orientation.VERTICAL   ? start.getRow() + length - 1 : start.getRow();
        int endCol = orientation == Orientation.HORIZONTAL ? start.getCol() + length - 1 : start.getCol();

        Coordinate end = new Coordinate(endRow, endCol);

        if (!board.isWithinBounds(start) || !board.isWithinBounds(end)) {
            return PlacementResult.failure(PlacementResult.Reason.OUT_OF_BOUNDS);
        }

        // ---- Overlap check via Board#canPlaceShip -----------------------
        if (!board.canPlaceShip(ship, start, orientation)) {
            return PlacementResult.failure(PlacementResult.Reason.OVERLAPS_SHIP);
        }

        return PlacementResult.success();
    }

    public PlacementResult validatePlacement(Board board, int size, int startX, int startY, boolean horizontal) {
        return null;
    }

    @Override
    public ShotResult resolveShot(Board board, Coordinate target) {
        AttackResult result = board.attack(target);

        switch (result) {
            case ALREADY_HIT:
                return ShotResult.alreadyShot(target);

            case MISS:
                return ShotResult.miss(target);

            case HIT: {
                Ship hitShip = board.getCell(target).getShip();
                return ShotResult.hit(target, hitShip);
            }

            case SUNK: {
                Ship sunkShip = board.getCell(target).getShip();
                return ShotResult.sunk(target, sunkShip);
            }

            default:
                // Defensive: unknown AttackResult — treat as miss
                return ShotResult.miss(target);
        }
    }

    @Override
    public boolean hasWon(Board board) {
        return board.allShipsSunk();
    }
}
