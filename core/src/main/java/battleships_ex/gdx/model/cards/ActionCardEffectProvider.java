package battleships_ex.gdx.model.cards;

/**
 * Central registry for all card effects.
 * This singleton allows Action Cards to access game-state-modifying logic
 * without being tightly coupled to the implementation (e.g., RulesEngine).
 */
public class ActionCardEffectProvider {

    private static ActionCardEffectProvider instance;
    private ActionCardEffect effects;

    private ActionCardEffectProvider() {}

    public static synchronized ActionCardEffectProvider getInstance() {
        if (instance == null) {
            instance = new ActionCardEffectProvider();
        }
        return instance;
    }

    public ActionCardEffect getEffects() {
        if (effects == null) {
            throw new IllegalStateException("ActionCardEffect implementation not registered.");
        }
        return effects;
    }

    public void setEffects(ActionCardEffect effects) {
        this.effects = effects;
    }
}
