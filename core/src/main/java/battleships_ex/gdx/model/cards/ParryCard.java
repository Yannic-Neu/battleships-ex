package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

public class ParryCard implements ActionCard {

    @Override
    public boolean canUse(Player user, Player opponent) {
        return true;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        user.setParryActive(true); // You must add this field + methods to Player
        return ActionCardResult.noEffect("Parry");
    }

    @Override
    public int getEnergyCost() {
        return 2; // pick any value you want
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
