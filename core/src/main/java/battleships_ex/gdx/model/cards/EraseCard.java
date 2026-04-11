package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

public class EraseCard implements ActionCard {

    @Override
    public boolean canUse(Player user, Player opponent) {
        return true;  // Always allowed unless you want extra conditions
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        // TODO: implement removing a revealed cell from user board
        // For now, just return NO_EFFECT
        return ActionCardResult.noEffect("Erase");
    }

    @Override
    public int getEnergyCost() {
        return 1; // choose balance later
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
