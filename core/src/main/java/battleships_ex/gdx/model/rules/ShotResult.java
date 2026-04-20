package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public final class ShotResult {

    public enum Outcome {
        ALREADY_SHOT,
        MISS,
        HIT,
        SUNK,
        MINE_HIT,
        BLOCKED
    }

    private final Outcome    outcome;
    private final Coordinate coordinate;
    private final Ship       sunkShip;   // non-null only when outcome == SUNK
    private final java.util.List<ShotResult> mineDetonationResults; // for MINE_HIT

    private ShotResult(Outcome outcome, Coordinate coordinate, Ship sunkShip) {
        this(outcome, coordinate, sunkShip, null);
    }

    private ShotResult(Outcome outcome, Coordinate coordinate, Ship sunkShip, java.util.List<ShotResult> mineDetonationResults) {
        this.outcome    = outcome;
        this.coordinate = coordinate;
        this.sunkShip   = sunkShip;
        this.mineDetonationResults = mineDetonationResults;
    }

    // ---- Factory methods ----------------------------------------------------

    public static ShotResult alreadyShot(Coordinate coordinate) {
        return new ShotResult(Outcome.ALREADY_SHOT, coordinate, null);
    }

    public static ShotResult miss(Coordinate coordinate) {
        return new ShotResult(Outcome.MISS, coordinate, null);
    }

    public static ShotResult hit(Coordinate coordinate, Ship ship) {
        return new ShotResult(Outcome.HIT, coordinate, null);
    }

    public static ShotResult sunk(Coordinate coordinate, Ship ship) {
        if (ship == null) throw new IllegalArgumentException("Sunk ship must not be null");
        return new ShotResult(Outcome.SUNK, coordinate, ship);
    }

    public static ShotResult mineHit(Coordinate coordinate, java.util.List<ShotResult> detonationResults) {
        return new ShotResult(Outcome.MINE_HIT, coordinate, null, detonationResults);
    }

    // ---- Accessors ----------------------------------------------------------

    public Outcome getOutcome() { return outcome; }

    public Coordinate getCoordinate() { return coordinate; }

    /** @return the sunk ship, or null if outcome != SUNK */
    public Ship getSunkShip() { return sunkShip; }

    public java.util.List<ShotResult> getMineDetonationResults() {
        return mineDetonationResults != null ? java.util.Collections.unmodifiableList(mineDetonationResults) : java.util.Collections.emptyList();
    }

    // ---- Convenience predicates ---------------------------------------------

    public boolean isHitOrSunk() {
        return outcome == Outcome.HIT || outcome == Outcome.SUNK || outcome == Outcome.MINE_HIT;
    }

    public boolean isShipHit() {
        return outcome == Outcome.HIT || outcome == Outcome.SUNK;
    }

    public boolean isSunk()  { return outcome == Outcome.SUNK; }
    public boolean isMiss()  { return outcome == Outcome.MISS; }
    public boolean isMineHit() { return outcome == Outcome.MINE_HIT; }
    public boolean isBlocked() { return outcome == Outcome.BLOCKED; }

    @Override
    public String toString() {
        return "ShotResult{outcome=" + outcome
            + ", coordinate=" + coordinate
            + (sunkShip != null ? ", sunkShip=" + sunkShip.getLength() + "-cell" : "")
            + (mineDetonationResults != null ? ", mineDetonationResults=" + mineDetonationResults.size() : "")
            + "}";
    }
}
