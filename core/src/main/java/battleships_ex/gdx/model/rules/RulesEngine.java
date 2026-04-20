package battleships_ex.gdx.model.rules;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public interface RulesEngine extends battleships_ex.gdx.model.cards.ActionCardEffect {

    PlacementResult validatePlacement(Board board,
                                      Ship ship,
                                      Coordinate start,
                                      Orientation orientation);

    ShotResult resolveShot(Board board, Coordinate target);

    boolean hasWon(Board board);
}
