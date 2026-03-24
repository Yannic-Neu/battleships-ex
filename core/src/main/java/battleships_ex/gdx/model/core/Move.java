package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Coordinate;

/**
 * Represents a single move (attack) made by a player during a game.
 */
public class Move {

    private final Coordinate position;
    private final boolean result;

    public Move(Coordinate position, boolean result) {
        this.position = position;
        this.result = result;
    }

    public Coordinate getPosition() {
        return position;
    }

    /**
     * @return true if the move was a hit, false if it was a miss
     */
    public boolean isHit() {
        return result;
    }
}
