package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Template for implementing a concrete ActionCard strategy.
 * Replace "TemplateCard" with your actual card name (e.g., ReconCard, MissileCard).
 * Implement the game logic specific to your card in the execute() method.
 */
public class TemplateCard implements ActionCard {

    @Override
    public boolean canUse(Player user, Player opponent) {
        // TODO: Define when this card can be played
        // Examples:
        // - Check if opponent has a valid board
        // - Check if opponent has unhit cells
        // - Check if a specific condition is met

        Board opponentBoard = opponent.getBoard();
        return opponentBoard != null && opponentBoard.hasValidTargets();
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent) {
        Board opponentBoard = opponent.getBoard();

        // Validate preconditions
        if (opponentBoard == null || !opponentBoard.hasValidTargets()) {
            return ActionCardResult.noEffect("TemplateCard");
        }

        // TODO: Implement your card's logic here
        List<Coordinate> affectedCoordinates = new ArrayList<>();
        boolean anyHit = false;
        boolean anyShipSunk = false;

        // Example: Select target(s) and perform action
        // Coordinate target = selectTarget(opponentBoard);
        // boolean isHit = opponentBoard.fireAt(target);
        // ...

        // TODO: Collect affected coordinates
        // affectedCoordinates.add(target);

        // Handle outcome
        return ActionCardResult.noEffect("TemplateCard");

    }

    @Override
    public int getEnergyCost() {
        return 0; // default
    }

    @Override
    public boolean endsTurn() {
        return false;
    }

    @Override
    public boolean allowsFireAfterUse() {
        return true;
    }

    /*
      TODO: Add helper methods specific to your card's logic
      Examples:
      - selectTargets()
      - validateTargets()
      - calculateAffectedArea()
     */
}
