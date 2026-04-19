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
        // This is the standard shot resolution. Action cards might use shootTile instead.
        // We can refactor this to use common logic if needed.
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
            return ShotResult.miss(coord); // Or handle error
        }

        // Check shield
        if (opponent != null && opponent.hasShield()) {
            opponent.consumeShield();
            return ShotResult.blocked(coord);
        }

        // Check already hit
        if (board.getCell(coord).isHit()) {
            return ShotResult.alreadyShot(coord);
        }

        // Check mine
        if (board.hasMine(coord)) {
            board.removeMine(coord);
            board.attack(coord); // Mark as hit
            // Trigger detonation: 2 random shots at the attacker
            // Wait, who is the attacker? We need the user player.
            // In this architecture, 'opponent' is the one being shot.
            // The detonation hits the 'user' (the one who triggered the mine).
            // But shootTile only takes 'opponent'.
            // The plan says: "effects.triggerRandomShots(opponent, 2)" in RulesEngine.shootTile logic.
            // But wait, if Player A shoots Player B's mine, Player A should be hit.
            // triggerRandomShots(opponent, 2) would hit Player B again? That's wrong.
            // "Execute 2 random shots at attacker's board"
            // We might need to pass the attacker to shootTile or handle detonation differently.
            
            // For now, let's assume triggerRandomShots targets the current player if called from here,
            // or we need to rethink the interface.
            // Actually, the ActionCardEffect is used by cards.
            // If a card shoots a mine, the card user gets hit.
            
            // Let's implement triggerRandomShots first.
            return ShotResult.mineHit(coord, null); // Detonation results handled by caller or GameController
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
                // EMPTY in the plan means: no ship, no mine, no shot
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
