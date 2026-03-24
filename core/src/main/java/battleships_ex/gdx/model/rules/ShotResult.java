package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public final class ShotResult {

    public enum Outcome {
        ALREADY_SHOT,
        MISS,
        HIT,
        SUNK
    }

    private final Outcome    outcome;
    private final Coordinate coordinate;
    private final Ship       sunkShip;   // non-null only when outcome == SUNK

    private ShotResult(Outcome outcome, Coordinate coordinate, Ship sunkShip) {
        this.outcome    = outcome;
        this.coordinate = coordinate;
        this.sunkShip   = sunkShip;
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

    // ---- Accessors ----------------------------------------------------------

    public Outcome getOutcome() { return outcome; }

    public Coordinate getCoordinate() { return coordinate; }

    /** @return the sunk ship, or null if outcome != SUNK */
    public Ship getSunkShip() { return sunkShip; }

    // ---- Convenience predicates ---------------------------------------------

    public boolean isHitOrSunk() {
        return outcome == Outcome.HIT || outcome == Outcome.SUNK;
    }

    public boolean isSunk()  { return outcome == Outcome.SUNK; }
    public boolean isMiss()  { return outcome == Outcome.MISS; }

    @Override
    public String toString() {
        return "ShotResult{outcome=" + outcome
            + ", coordinate=" + coordinate
            + (sunkShip != null ? ", sunkShip=" + sunkShip.getLength() + "-cell" : "")
            + "}";
    }
}
