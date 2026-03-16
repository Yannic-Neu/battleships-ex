package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StandardRulesEngine}.
 *
 * Uses the real Board/Cell/Ship/Coordinate classes (no mocks needed because
 * the domain objects are simple and fast). Each test is fully self-contained:
 * fresh instances are created in @Before so no state leaks between cases.
 *
 * Ship construction: Board.placeShip(startX, startY, size, horizontal) creates
 * and registers the Ship internally. Retrieve a Ship reference after placement
 * via board.getShips() when needed.
 *
 * Orientation: represented as boolean (true = horizontal) until the Orientation
 * enum is introduced. Update call sites to the enum at that point.
 */
public class RulesEngineTest {

    private StandardRulesEngine engine;
    private Board board;

    @Before
    public void setUp() {
        engine = new StandardRulesEngine();
        board  = new Board();
    }

    // =========================================================================
    // validatePlacement
    // =========================================================================

    @Test
    public void placement_validHorizontal_returnsSuccess() {
        PlacementResult result = engine.validatePlacement(board, 3, 0, 0, true);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_validVertical_returnsSuccess() {
        PlacementResult result = engine.validatePlacement(board, 3, 5, 5, false);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_horizontalOutOfBoundsRight_returnsOutOfBounds() {
        // Size-4 ship at column 8 → cols 8,9,10,11 — overshoots right edge
        PlacementResult result = engine.validatePlacement(board, 4, 8, 0, true);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_verticalOutOfBoundsBottom_returnsOutOfBounds() {
        // Size-3 ship at row 9 → rows 9,10,11 — overshoots bottom edge
        PlacementResult result = engine.validatePlacement(board, 3, 0, 9, false);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_negativeStart_returnsOutOfBounds() {
        PlacementResult result = engine.validatePlacement(board, 2, -1, 0, true);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_overlapsExistingShip_returnsOverlap() {
        // Place a 2-cell ship at (3,3)-(4,3), then try placing on same cells
        board.placeShip(3, 3, 2, true);

        PlacementResult result = engine.validatePlacement(board, 2, 3, 3, true);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OVERLAPS_SHIP, result.getReason());
    }

    @Test
    public void placement_adjacentRow_returnsTooClose() {
        // Ship at (3,3)-(4,3); new ship at (3,4) is 1 row below — touching
        board.placeShip(3, 3, 2, true);

        PlacementResult result = engine.validatePlacement(board, 2, 3, 4, true);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.TOO_CLOSE_TO_SHIP, result.getReason());
    }

    @Test
    public void placement_adjacentColumn_returnsTooClose() {
        // Ship at (3,3)-(4,3); new ship at (5,3) starts 1 col to the right — touching
        board.placeShip(3, 3, 2, true);

        PlacementResult result = engine.validatePlacement(board, 1, 5, 3, true);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.TOO_CLOSE_TO_SHIP, result.getReason());
    }

    @Test
    public void placement_twoRowsAway_returnsSuccess() {
        // Ship at (3,3)-(4,3); new ship at (3,5) — 2 rows below, outside buffer
        board.placeShip(3, 3, 2, true);

        PlacementResult result = engine.validatePlacement(board, 2, 3, 5, true);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_cornerOfBoard_returnsSuccess() {
        // 1-cell ship at (9,9) — should fit exactly in the corner
        PlacementResult result = engine.validatePlacement(board, 1, 9, 9, true);
        assertTrue(result.isValid());
    }

    // =========================================================================
    // resolveShot
    // =========================================================================

    @Test
    public void resolveShot_emptyBoard_returnsMiss() {
        ShotResult result = engine.resolveShot(board, new Coordinate(0, 0));
        assertEquals(ShotResult.Outcome.MISS, result.getOutcome());
    }

    @Test
    public void resolveShot_hitShipNotSunk_returnsHit() {
        // 2-cell ship; hit only the first cell
        board.placeShip(0, 0, 2, true);

        ShotResult result = engine.resolveShot(board, new Coordinate(0, 0));
        assertEquals(ShotResult.Outcome.HIT, result.getOutcome());
        assertNull("SunkShip should be null for a non-sinking hit", result.getSunkShip());
    }

    @Test
    public void resolveShot_allCellsHit_returnsSunk() {
        // 1-cell ship — one shot sinks it
        board.placeShip(4, 4, 1, true);

        ShotResult result = engine.resolveShot(board, new Coordinate(4, 4));
        assertEquals(ShotResult.Outcome.SUNK, result.getOutcome());
        assertNotNull("SunkShip must be non-null on SUNK outcome", result.getSunkShip());
    }

    @Test
    public void resolveShot_sinkMultiCellShip_requiresAllCells() {
        // 3-cell ship at (2,2),(3,2),(4,2); sink it by hitting all three
        board.placeShip(2, 2, 3, true);

        engine.resolveShot(board, new Coordinate(2, 2));  // HIT
        engine.resolveShot(board, new Coordinate(3, 2));  // HIT
        ShotResult last = engine.resolveShot(board, new Coordinate(4, 2)); // SUNK

        assertEquals(ShotResult.Outcome.SUNK, last.getOutcome());
        assertNotNull(last.getSunkShip());
        assertEquals(3, last.getSunkShip().getSize());
    }

    @Test
    public void resolveShot_duplicateShot_returnsAlreadyShot_noBoardMutation() {
        // First shot: miss. Second shot at same coordinate: ALREADY_SHOT.
        // Board must NOT throw on the second call because engine guards first.
        engine.resolveShot(board, new Coordinate(5, 5));

        ShotResult second = engine.resolveShot(board, new Coordinate(5, 5));
        assertEquals(ShotResult.Outcome.ALREADY_SHOT, second.getOutcome());
    }

    @Test
    public void resolveShot_missDoesNotAffectShip() {
        board.placeShip(7, 7, 2, true);
        engine.resolveShot(board, new Coordinate(0, 0));  // miss elsewhere

        Ship ship = board.getShips().get(0);
        assertFalse("Ship must not be sunk after a miss elsewhere", ship.isSunk());
    }

    @Test
    public void resolveShot_coordinatePreservedInResult() {
        Coordinate target = new Coordinate(3, 6);
        ShotResult result = engine.resolveShot(board, target);
        assertEquals(target, result.getCoordinate());
    }

    // =========================================================================
    // hasWon
    // =========================================================================

    @Test
    public void hasWon_shipsPlacedNoneHit_returnsFalse() {
        board.placeShip(0, 0, 2, true);
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_shipPartiallyHit_returnsFalse() {
        board.placeShip(0, 0, 2, true);
        engine.resolveShot(board, new Coordinate(0, 0));  // hit only first cell
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_allShipsSunk_returnsTrue() {
        board.placeShip(0, 0, 1, true);
        engine.resolveShot(board, new Coordinate(0, 0));  // sinks the only ship
        assertTrue(engine.hasWon(board));
    }

    @Test
    public void hasWon_emptyBoard_returnsFalse() {
        // Board#allShipsSunk() returns false when ships list is empty.
        // An empty board must never be considered a win.
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_multipleShips_allMustBeSunkToWin() {
        board.placeShip(0, 0, 1, true);
        board.placeShip(5, 5, 1, true);

        engine.resolveShot(board, new Coordinate(0, 0));  // sinks first ship
        assertFalse("Second ship still afloat — should not be a win yet",
            engine.hasWon(board));

        engine.resolveShot(board, new Coordinate(5, 5));  // sinks second ship
        assertTrue(engine.hasWon(board));
    }
}
