package battleships_ex.gdx.model.rules;

/**
 * Immutable value object returned by {@link RulesEngine#validatePlacement}.
 *
 * Carries a boolean success flag and, on failure, a {@link Reason} enum value
 * that tells the View exactly why placement was rejected — without the View
 * needing to parse strings or re-run any logic itself.
 */
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

    // ---- Factory methods ----------------------------------------------------

    public static PlacementResult success() {
        return new PlacementResult(true, null);
    }

    public static PlacementResult failure(Reason reason) {
        if (reason == null) throw new IllegalArgumentException("Failure reason must not be null");
        return new PlacementResult(false, reason);
    }

    // ---- Accessors ----------------------------------------------------------

    /** @return true if the placement is legal and may be applied to the board */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return why the placement was rejected, or {@code null} when valid.
     *         Always check {@link #isValid()} before calling this.
     */
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
