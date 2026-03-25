package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.core.Player;

public interface ActionCard {

    String getDisplayName();
    String getDescription();

    boolean canUse(Player user, Player opponent);

    ActionCardResult execute(Player user, Player opponent);
}
