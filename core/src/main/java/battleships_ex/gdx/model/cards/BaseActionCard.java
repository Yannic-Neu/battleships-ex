package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

/**
 * Common base class for Action Cards to handle energy and usage limits.
 */
public abstract class BaseActionCard implements ActionCard {

    protected final String cardName;
    protected final int energyCost;

    protected BaseActionCard(String cardName, int energyCost) {
        this.cardName = cardName;
        this.energyCost = energyCost;
    }

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean canUse(Player user, Player opponent) {
        return user.getEnergy() >= energyCost;
    }

    protected void consumeUse(Player user) {
        user.spendEnergy(energyCost);
    }

    @Override
    public boolean endsTurn() {
        return false;
    }
}
