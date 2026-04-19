package battleships_ex.gdx.model.cards;

import java.util.Collections;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;

/**
 * Reveal adjacent ship/mine count (Minesweeper-style).
 */
public class SonarCard extends BaseActionCard {

    public SonarCard() {
        super("Sonar", 2, 2);
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent, Coordinate target) {
        if (target == null) throw new IllegalArgumentException("Target required for Sonar");
        if (!canUse(user, opponent)) return ActionCardResult.noEffect(cardName);

        ActionCardEffect effects = ActionCardEffectProvider.getInstance().getEffects();
        
        // Mark as scanned and get info
        effects.revealTileInfo(opponent, target);
        int adjCount = opponent.getBoard().countAdjacentOccupancy(target);

        consumeUse(user);

        ActionCardResult result = ActionCardResult.revealed(cardName, Collections.singletonList(target));
        result.setMetadata("adjacentCount", adjCount);
        return result;
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
