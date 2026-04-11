package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

public class ShieldCard implements ActionCard {

    private static final int ENERGY_COST = 2;

    @Override
    public boolean canUse(Player user, Player opponent) {
        // You cannot activate shield twice
        return !user.hasShield();
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        user.activateShield();
        return ActionCardResult.noEffect("Shield");
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
