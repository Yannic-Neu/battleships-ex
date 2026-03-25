package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Coordinate;

import java.util.Collections;
import java.util.List;

public final class ActionCardResult {

    public enum Outcome {
        /** Card had no effect (all targets already hit, no valid coordinates, etc.) */
        NO_EFFECT,

        /** Card revealed information without attacking (e.g. RECON) */
        REVEALED,

        /** Card caused one or more hits but no ships were sunk */
        HIT,

        /** Card sunk one or more ships */
        SUNK
    }

    private final Outcome        outcome;
    private final String         cardName;
    private final List<Coordinate> affectedCoordinates;   // cells revealed or hit

    private ActionCardResult(Outcome outcome,
                             String cardName,
                             List<Coordinate> affectedCoordinates) {
        this.outcome             = outcome;
        this.cardName            = cardName;
        this.affectedCoordinates = Collections.unmodifiableList(affectedCoordinates);
    }

    // ---- Factory methods ----------------------------------------------------

    public static ActionCardResult noEffect(String cardName) {
        return new ActionCardResult(Outcome.NO_EFFECT, cardName, Collections.emptyList());
    }

    public static ActionCardResult revealed(String cardName, List<Coordinate> coordinates) {
        return new ActionCardResult(Outcome.REVEALED, cardName, coordinates);
    }

    public static ActionCardResult hit(String cardName, List<Coordinate> coordinates) {
        return new ActionCardResult(Outcome.HIT, cardName, coordinates);
    }

    public static ActionCardResult sunk(String cardName, List<Coordinate> coordinates) {
        return new ActionCardResult(Outcome.SUNK, cardName, coordinates);
    }

    // ---- Accessors ----------------------------------------------------------

    public Outcome getOutcome() { return outcome; }

    public String getCardName() { return cardName; }

    public List<Coordinate> getAffectedCoordinates() { return affectedCoordinates; }

    public boolean hadEffect() { return outcome != Outcome.NO_EFFECT; }

    @Override
    public String toString() {
        return "ActionCardResult{outcome=" + outcome
            + ", card=" + cardName
            + ", affected=" + affectedCoordinates.size() + " cells}";
    }
}
