package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;

public interface ActionCard {

    boolean canUse(Player user, Player opponent);

    ActionCardResult execute(Player user, Player opponent, battleships_ex.gdx.model.board.Coordinate target);

    int getEnergyCost();
    boolean endsTurn();
}
