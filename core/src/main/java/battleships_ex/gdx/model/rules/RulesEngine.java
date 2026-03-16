package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;


public interface RulesEngine {
    PlacementResult validatePlacement(Board board, int size, int startX, int startY, boolean horizontal );

    ShotResult resolveShot(Board board, Coordinate target);
    boolean hasWon(Board board);
}
