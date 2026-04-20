package battleships_ex.gdx.model.cards;

import java.util.Collections;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;

/**
 * Place a defensive mine on your own board.
 */
public class MineCard extends BaseActionCard {

    public MineCard() {
        super("Mine", 1);
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent, Coordinate target) {
        if (target == null) throw new IllegalArgumentException("Target required for Mine");
        if (!canUse(user, opponent)) return ActionCardResult.noEffect(cardName);

        ActionCardEffect effects = ActionCardEffectProvider.getInstance().getEffects();
        ActionCardEffect.MinePlacementResult placementResult = effects.placeMineOnOwnBoard(user, target);

        if (placementResult == ActionCardEffect.MinePlacementResult.SUCCESS) {
            consumeUse(user);
            return ActionCardResult.revealed(cardName, Collections.singletonList(target));
        } else {
            return ActionCardResult.noEffect(cardName);
        }
    }

    @Override
    public boolean endsTurn() {
        return false;
    }
}
