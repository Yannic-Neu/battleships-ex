package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

public class DoubleShotCard implements ActionCard {

    @Override
    public boolean canUse(Player user, Player opponent) {
        return true;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        return ActionCardResult.noEffect("DoubleShot");
    }

    @Override public int getEnergyCost() { return 2; }
    @Override public boolean endsTurn() { return false; }
    @Override public boolean allowsFireAfterUse() { return true; }
}
