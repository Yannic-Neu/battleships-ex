package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StandardRulesEngine}.
 *
 * Uses the real Board / Cell / Ship / Coordinate classes.
 * Each test is self-contained — fresh instances created in @Before.
 *
 * API alignment:
 *   - Board(width, height)
 *   - Board.placeShip(Ship, Coordinate, Orientation)
 *   - Board.canPlaceShip(Ship, Coordinate, Orientation)
 *   - Ship(ShipType, Orientation)
 *   - Coordinate(row, col)
 *   - Orientation.HORIZONTAL / VERTICAL
 */
public class RulesEngineTest {

    // Standard 10×10 board
    private static final int SIZE = 10;

    private StandardRulesEngine engine;
    private Board board;

    @Before
    public void setUp() {
        engine = new StandardRulesEngine();
        board  = new Board(SIZE, SIZE);
    }

    // =========================================================================
    // validatePlacement
    // =========================================================================

    @Test
    public void placement_validHorizontal_returnsSuccess() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_validVertical_returnsSuccess() {
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.VERTICAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(0, 0), Orientation.VERTICAL);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_horizontalOutOfBoundsRight_returnsOutOfBounds() {
        // BATTLESHIP length 4, starting at col 8 → cols 8,9,10,11 — overshoots
        Ship ship = new Ship(ShipType.BATTLESHIP, Orientation.HORIZONTAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(0, 8), Orientation.HORIZONTAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_verticalOutOfBoundsBottom_returnsOutOfBounds() {
        // SUBMARINE length 3, starting at row 9 → rows 9,10,11 — overshoots
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.VERTICAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(9, 0), Orientation.VERTICAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_overlapsExistingShip_returnsOverlap() {
        // Place a destroyer at (0,0), then try to place another on the same cell
        Ship first  = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        Ship second = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(first, new Coordinate(0, 0), Orientation.HORIZONTAL);

        PlacementResult result = engine.validatePlacement(
            board, second, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OVERLAPS_SHIP, result.getReason());
    }

    @Test
    public void placement_noOverlap_returnsSuccess() {
        Ship first  = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        Ship second = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(first, new Coordinate(0, 0), Orientation.HORIZONTAL);

        // Place second ship well clear of the first
        PlacementResult result = engine.validatePlacement(
            board, second, new Coordinate(5, 5), Orientation.HORIZONTAL);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_nullArguments_returnsOutOfBounds() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        PlacementResult result = engine.validatePlacement(board, ship, null, Orientation.HORIZONTAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
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
        // DESTROYER length 2 — hit only the first cell
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        ShotResult result = engine.resolveShot(board, new Coordinate(0, 0));
        assertEquals(ShotResult.Outcome.HIT, result.getOutcome());
        assertNull("SunkShip must be null for a non-sinking hit", result.getSunkShip());
    }

    @Test
    public void resolveShot_sinkSingleCellShip_returnsSunk() {
        // PATROL / smallest ship — one shot sinks it
        Ship ship = new Ship(ShipType.PATROL_BOAT, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(4, 4), Orientation.HORIZONTAL);

        ShotResult result = engine.resolveShot(board, new Coordinate(4, 4));
        assertEquals(ShotResult.Outcome.SUNK, result.getOutcome());
        assertNotNull("SunkShip must be non-null on SUNK outcome", result.getSunkShip());
    }

    @Test
    public void resolveShot_sinkMultiCellShip_requiresAllCells() {
        // SUBMARINE length 3 at row 2, cols 2–4
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(2, 2), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(2, 2));  // HIT
        engine.resolveShot(board, new Coordinate(2, 3));  // HIT
        ShotResult last = engine.resolveShot(board, new Coordinate(2, 4)); // SUNK

        assertEquals(ShotResult.Outcome.SUNK, last.getOutcome());
        assertNotNull(last.getSunkShip());
        assertEquals(3, last.getSunkShip().getLength());
    }

    @Test
    public void resolveShot_duplicateShot_returnsAlreadyShot() {
        // Miss once, then fire at the same cell again
        engine.resolveShot(board, new Coordinate(5, 5));
        ShotResult second = engine.resolveShot(board, new Coordinate(5, 5));
        assertEquals(ShotResult.Outcome.ALREADY_SHOT, second.getOutcome());
    }

    @Test
    public void resolveShot_missDoesNotAffectShip() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(7, 7), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(0, 0));  // miss elsewhere
        assertFalse("Ship must not be sunk after a miss elsewhere", ship.isSunk());
    }

    @Test
    public void resolveShot_coordinatePreservedInResult() {
        Coordinate target = new Coordinate(3, 6);
        ShotResult result  = engine.resolveShot(board, target);
        assertEquals(target, result.getCoordinate());
    }

    // =========================================================================
    // hasWon
    // =========================================================================

    @Test
    public void hasWon_shipsPlacedNoneHit_returnsFalse() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_shipPartiallyHit_returnsFalse() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);
        engine.resolveShot(board, new Coordinate(0, 0));  // hit only first cell
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_allShipsSunk_returnsTrue() {
        Ship ship = new Ship(ShipType.PATROL_BOAT, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);
        engine.resolveShot(board, new Coordinate(0, 0));
        assertTrue(engine.hasWon(board));
    }

    @Test
    public void hasWon_emptyBoard_returnsFalse() {
        // Board#allShipsSunk() returns false when ships list is empty
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_multipleShips_allMustBeSunkToWin() {
        Ship s1 = new Ship(ShipType.PATROL_BOAT, Orientation.HORIZONTAL);
        Ship s2 = new Ship(ShipType.PATROL_BOAT, Orientation.HORIZONTAL);
        board.placeShip(s1, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(s2, new Coordinate(5, 5), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(0, 0));  // sinks s1
        assertFalse("s2 still afloat — must not be a win yet", engine.hasWon(board));

        engine.resolveShot(board, new Coordinate(5, 5));  // sinks s2
        assertTrue(engine.hasWon(board));
    }
}
