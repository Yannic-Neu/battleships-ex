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
 */
public class RulesEngineTest {

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
        Ship ship = new Ship(ShipType.CARRIER, Orientation.HORIZONTAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(0, 8), Orientation.HORIZONTAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_verticalOutOfBoundsBottom_returnsOutOfBounds() {
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.VERTICAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, new Coordinate(9, 0), Orientation.VERTICAL);
        assertFalse(result.isValid());
        assertEquals(PlacementResult.Reason.OUT_OF_BOUNDS, result.getReason());
    }

    @Test
    public void placement_overlapsExistingShip_returnsOverlap() {
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

        PlacementResult result = engine.validatePlacement(
            board, second, new Coordinate(5, 5), Orientation.HORIZONTAL);
        assertTrue(result.isValid());
    }

    @Test
    public void placement_nullCoordinate_returnsOutOfBounds() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        PlacementResult result = engine.validatePlacement(
            board, ship, null, Orientation.HORIZONTAL);
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
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);

        ShotResult result = engine.resolveShot(board, new Coordinate(0, 0));
        assertEquals(ShotResult.Outcome.HIT, result.getOutcome());
        assertNull(result.getSunkShip());
    }

    @Test
    public void resolveShot_sinkPatrolShip_returnsSunk() {
        // PATROL length 2 — occupies (4,4) and (4,5)
        Ship ship = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(4, 4), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(4, 4));  // HIT
        ShotResult result = engine.resolveShot(board, new Coordinate(4, 5)); // SUNK
        assertEquals(ShotResult.Outcome.SUNK, result.getOutcome());
        assertNotNull(result.getSunkShip());
    }

    @Test
    public void resolveShot_sinkMultiCellShip_requiresAllCells() {
        Ship ship = new Ship(ShipType.SUBMARINE, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(2, 2), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(2, 2));
        engine.resolveShot(board, new Coordinate(2, 3));
        ShotResult last = engine.resolveShot(board, new Coordinate(2, 4));

        assertEquals(ShotResult.Outcome.SUNK, last.getOutcome());
        assertNotNull(last.getSunkShip());
        assertEquals(3, last.getSunkShip().getLength());
    }

    @Test
    public void resolveShot_duplicateShot_returnsAlreadyShot() {
        engine.resolveShot(board, new Coordinate(5, 5));
        ShotResult second = engine.resolveShot(board, new Coordinate(5, 5));
        assertEquals(ShotResult.Outcome.ALREADY_SHOT, second.getOutcome());
    }

    @Test
    public void resolveShot_missDoesNotAffectShip() {
        Ship ship = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(7, 7), Orientation.HORIZONTAL);

        engine.resolveShot(board, new Coordinate(0, 0));
        assertFalse(ship.isSunk());
    }

    @Test
    public void resolveShot_coordinatePreservedInResult() {
        Coordinate target = new Coordinate(3, 6);
        assertEquals(target, engine.resolveShot(board, target).getCoordinate());
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
        engine.resolveShot(board, new Coordinate(0, 0));
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_allShipsSunk_returnsTrue() {
        // PATROL length 2 — must hit both cells to sink
        Ship ship = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        board.placeShip(ship, new Coordinate(0, 0), Orientation.HORIZONTAL);
        engine.resolveShot(board, new Coordinate(0, 0));
        engine.resolveShot(board, new Coordinate(0, 1));
        assertTrue(engine.hasWon(board));
    }

    @Test
    public void hasWon_emptyBoard_returnsFalse() {
        assertFalse(engine.hasWon(board));
    }

    @Test
    public void hasWon_multipleShips_allMustBeSunkToWin() {
        // PATROL length 2 — place ships far enough apart to avoid overlap
        Ship s1 = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        Ship s2 = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        board.placeShip(s1, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(s2, new Coordinate(5, 5), Orientation.HORIZONTAL);

        // Sink s1 fully
        engine.resolveShot(board, new Coordinate(0, 0));
        engine.resolveShot(board, new Coordinate(0, 1));
        assertFalse(engine.hasWon(board));  // s2 still afloat

        // Sink s2 fully
        engine.resolveShot(board, new Coordinate(5, 5));
        engine.resolveShot(board, new Coordinate(5, 6));
        assertTrue(engine.hasWon(board));
    }
}
