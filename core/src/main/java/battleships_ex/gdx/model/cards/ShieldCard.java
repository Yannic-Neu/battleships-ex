package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;

public class ShieldCard implements ActionCard {

    private static final int ENERGY_COST = 2;

    @Override
    public boolean canUse(Player user, Player opponent) {
        // ✅ Shield can only be placed if a target tile is selected
        return user.getPendingTarget() != null;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {

        Coordinate target = user.getPendingTarget();
        if (target != null) {
            user.addShieldedTile(target);
        }
        return ActionCardResult.noEffect("Shield placed");
    }

    @Override
    public int getEnergyCost() {
        return ENERGY_COST;
    }

    @Override
    public boolean endsTurn() {
        return true;
    }

    @Override
    public boolean allowsFireAfterUse() {
        return false;
    }
}
