package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

import java.util.Collections;

public class ScanCard implements ActionCard {

    private static final int ENERGY_COST = 1;

    @Override
    public boolean canUse(Player user, Player opponent) {
        return true;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        // Scan intent only – actual reveal handled elsewhere
        return ActionCardResult.revealed("Scan", Collections.emptyList());
    }

    @Override
    public int getEnergyCost() {
        return ENERGY_COST;
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
