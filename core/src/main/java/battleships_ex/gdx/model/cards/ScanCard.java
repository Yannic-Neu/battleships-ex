package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.core.Player;
import java.util.List;
import java.util.ArrayList;

import battleships_ex.gdx.model.board.Coordinate;


import java.util.Collections;

public class ScanCard implements ActionCard {

    private static final int ENERGY_COST = 3;

    @Override
    public boolean canUse(Player user, Player opponent) {
        return true;
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        List<Coordinate> revealed = new ArrayList<>();

        // example coordinates (you decide how)
        revealed.add(new Coordinate(4, 5));
        revealed.add(new Coordinate(4, 6));
        revealed.add(new Coordinate(5, 5));

        return ActionCardResult.revealed("Scan", revealed);
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
