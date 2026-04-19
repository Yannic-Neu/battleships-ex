package battleships_ex.gdx.model.cards;

import java.util.ArrayList;
import java.util.List;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.ShotResult;

/**
 * Shoot an entire row or column.
 * Target coordinate's row or col determines the strike zone.
 * Metadata can specify if it's a ROW or COLUMN strike.
 */
public class AirstrikeCard extends BaseActionCard {

    public enum Orientation {
        ROW, COLUMN
    }

    private Orientation orientation = Orientation.ROW; // Default

    public AirstrikeCard() {
        super("Airstrike", 4, 1);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void toggleOrientation() {
        this.orientation = (this.orientation == Orientation.ROW) ? Orientation.COLUMN : Orientation.ROW;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent, Coordinate target) {
        if (target == null) throw new IllegalArgumentException("Target required for Airstrike");
        if (!canUse(user, opponent)) return ActionCardResult.noEffect(cardName);

        List<Coordinate> targets = new ArrayList<>();
        if (orientation == Orientation.ROW) {
            int row = target.getRow();
            for (int col = 0; col < 10; col++) {
                targets.add(new Coordinate(row, col));
            }
        } else {
            int col = target.getCol();
            for (int row = 0; row < 10; row++) {
                targets.add(new Coordinate(row, col));
            }
        }

        ActionCardEffect effects = ActionCardEffectProvider.getInstance().getEffects();
        List<ShotResult> results = effects.shootArea(opponent, targets);

        consumeUse(user);

        boolean anySunk = results.stream().anyMatch(ShotResult::isSunk);
        boolean anyHit = results.stream().anyMatch(ShotResult::isShipHit);

        List<Coordinate> affected = new ArrayList<>();
        for (ShotResult sr : results) {
            affected.add(sr.getCoordinate());
        }

        if (anySunk) return ActionCardResult.sunk(cardName, affected);
        if (anyHit) return ActionCardResult.hit(cardName, affected);
        return ActionCardResult.revealed(cardName, affected);
    }

    @Override
    public boolean endsTurn() {
        return false;
    }

    @Override
    public boolean allowsFireAfterUse() {
        return true;
    }
}
