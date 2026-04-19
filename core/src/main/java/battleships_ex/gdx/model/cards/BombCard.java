package battleships_ex.gdx.model.cards;

import java.util.ArrayList;
import java.util.List;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.ShotResult;

/**
 * Shoot a 2x2 area.
 */
public class BombCard extends BaseActionCard {

    public BombCard() {
        super("Bomb", 3, 2);
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent, Coordinate target) {
        if (target == null) throw new IllegalArgumentException("Target (top-left) required for Bomb");
        if (!canUse(user, opponent)) return ActionCardResult.noEffect(cardName);

        List<Coordinate> area = new ArrayList<>();
        int r = target.getRow();
        int c = target.getCol();
        
        area.add(new Coordinate(r, c));
        area.add(new Coordinate(r + 1, c));
        area.add(new Coordinate(r, c + 1));
        area.add(new Coordinate(r + 1, c + 1));

        ActionCardEffect effects = ActionCardEffectProvider.getInstance().getEffects();
        List<ShotResult> results = effects.shootArea(opponent, area);

        consumeUse(user);

        boolean anySunk = results.stream().anyMatch(ShotResult::isSunk);
        boolean anyHit = results.stream().anyMatch(ShotResult::isHitOrSunk);

        List<Coordinate> affected = new ArrayList<>();
        for (ShotResult sr : results) {
            affected.add(sr.getCoordinate());
        }

        if (anySunk) return ActionCardResult.sunk(cardName, affected);
        if (anyHit) return ActionCardResult.hit(cardName, affected);
        return ActionCardResult.revealed(cardName, affected); // All misses
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
