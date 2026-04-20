package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StandardRulesEngine implements RulesEngine {

    private final Random random = new Random();

    @Override
    public PlacementResult validatePlacement(Board board,
                                             Ship ship,
                                             Coordinate start,
                                             Orientation orientation) {
        if (ship == null || start == null || orientation == null) {
            return PlacementResult.failure(PlacementResult.Reason.OUT_OF_BOUNDS);
        }

        int length = ship.getLength();
        int endRow = orientation == Orientation.VERTICAL   ? start.getRow() + length - 1 : start.getRow();
        int endCol = orientation == Orientation.HORIZONTAL ? start.getCol() + length - 1 : start.getCol();

        if (!board.isWithinBounds(start) || !board.isWithinBounds(endRow, endCol)) {
            return PlacementResult.failure(PlacementResult.Reason.OUT_OF_BOUNDS);
        }

        if (!board.canPlaceShip(ship, start, orientation)) {
            return PlacementResult.failure(PlacementResult.Reason.OVERLAPS_SHIP);
        }

        return PlacementResult.success();
    }

    @Override
    public ShotResult resolveShot(Board board, Coordinate target) {
        return shootTileInternal(null, board, target);
    }

    @Override
    public ShotResult shootTile(Player opponent, Coordinate coord) {
        if (opponent == null || opponent.getBoard() == null) {
            return ShotResult.miss(coord);
        }
        return shootTileInternal(opponent, opponent.getBoard(), coord);
    }

    private ShotResult shootTileInternal(Player opponent, Board board, Coordinate coord) {
        if (!board.isWithinBounds(coord)) {
            return ShotResult.miss(coord);
        }

        if (board.getCell(coord).isHit()) {
            return ShotResult.alreadyShot(coord);
        }

        // --- Mine Logic ---
        if (board.hasMine(coord)) {
            board.removeMine(coord);
            board.attack(coord); // Mark tile as hit

            // Note: The counter-shots (triggerRandomShots) must be triggered by the caller (GameController)
            // because the RulesEngine here doesn't have access to the attacker's board directly
            // to return a recursive ShotResult easily without more architecture changes.
            // We'll return MINE_HIT and let GameController handle the 2 random shots back at the attacker.
            return ShotResult.mineHit(coord, null);
        }

        AttackResult result = board.attack(coord);

        switch (result) {
            case MISS:
                return ShotResult.miss(coord);
            case HIT: {
                Ship hitShip = board.getCell(coord).getShip();
                return ShotResult.hit(coord, hitShip);
            }
            case SUNK: {
                Ship sunkShip = board.getCell(coord).getShip();
                return ShotResult.sunk(coord, sunkShip);
            }
            case ALREADY_HIT:
                return ShotResult.alreadyShot(coord);
            default:
                return ShotResult.miss(coord);
        }
    }

    @Override
    public List<ShotResult> shootArea(Player opponent, List<Coordinate> coords) {
        List<ShotResult> results = new ArrayList<>();
        for (Coordinate coord : coords) {
            if (opponent.getBoard().isWithinBounds(coord) && !opponent.getBoard().getCell(coord).isHit()) {
                results.add(shootTile(opponent, coord));
            }
        }
        return results;
    }

    @Override
    public TileInfo revealTileInfo(Player opponent, Coordinate coord) {
        Board board = opponent.getBoard();
        board.markScanned(coord);

        if (board.hasMine(coord)) return TileInfo.MINE;
        if (board.getCell(coord).hasShip()) return TileInfo.SHIP;
        return TileInfo.EMPTY;
    }

    @Override
    public MinePlacementResult placeMineOnOwnBoard(Player user, Coordinate coord) {
        Board board = user.getBoard();
        if (board.getCell(coord).isHit() || board.getCell(coord).hasShip() || board.hasMine(coord)) {
            return MinePlacementResult.INVALID_POSITION;
        }
        board.placeMine(coord);
        return MinePlacementResult.SUCCESS;
    }

    @Override
    public List<ShotResult> triggerRandomShots(Player opponent, int count) {
        List<ShotResult> results = new ArrayList<>();
        List<Coordinate> targets = opponent.getBoard().getUnhitTiles();
        if (targets.isEmpty()) return results;

        for (int i = 0; i < count && !targets.isEmpty(); i++) {
            Coordinate target = targets.remove(random.nextInt(targets.size()));
            results.add(shootTile(opponent, target));
        }
        return results;
    }

    @Override
    public List<Coordinate> getAvailableTiles(Player opponent, TileState state) {
        switch (state) {
            case UNHIT:
                return opponent.getBoard().getUnhitTiles();
            case UNOCCUPIED:
                return opponent.getBoard().getUnoccupiedTiles();
            case EMPTY:
                List<Coordinate> empty = new ArrayList<>();
                Board board = opponent.getBoard();
                for (Coordinate coord : board.getUnhitTiles()) {
                    if (!board.getCell(coord).hasShip() && !board.hasMine(coord)) {
                        empty.add(coord);
                    }
                }
                return empty;
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public boolean hasWon(Board board) {
        return board.allShipsSunk();
    }
}
