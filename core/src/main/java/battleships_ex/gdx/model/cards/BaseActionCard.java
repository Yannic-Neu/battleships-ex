package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

/**
 * Common base class for Action Cards to handle energy and usage limits.
 */
public abstract class BaseActionCard implements ActionCard {

    protected final String cardName;
    protected final int energyCost;
    protected final int maxUses;
    protected int remainingUses;

    protected BaseActionCard(String cardName, int energyCost, int maxUses) {
        this.cardName = cardName;
        this.energyCost = energyCost;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
    }

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean canUse(Player user, Player opponent) {
        return remainingUses > 0 && user.getEnergy() >= energyCost;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    protected void consumeUse(Player user) {
        remainingUses--;
        user.spendEnergy(energyCost);
    }
}
