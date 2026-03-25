package battleships_ex.gdx.model.rules;

public final class PlacementResult {

    public enum Reason {
        /** One or more cells of the ship footprint fall outside the grid. */
        OUT_OF_BOUNDS,

        /** The footprint directly overlaps a cell already occupied by a ship. */
        OVERLAPS_SHIP,

        /**
         * The footprint's 1-cell adjacency buffer overlaps an existing ship.
         * Ships are touching, which is illegal under standard Battleships rules.
         */
        TOO_CLOSE_TO_SHIP
    }

    private final boolean valid;
    private final Reason  reason;   // null iff valid == true

    private PlacementResult(boolean valid, Reason reason) {
        this.valid  = valid;
        this.reason = reason;
    }

    public static PlacementResult success() {
        return new PlacementResult(true, null);
    }

    public static PlacementResult failure(Reason reason) {
        if (reason == null) throw new IllegalArgumentException("Failure reason must not be null");
        return new PlacementResult(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return valid
            ? "PlacementResult{VALID}"
            : "PlacementResult{INVALID, reason=" + reason + "}";
    }
}
