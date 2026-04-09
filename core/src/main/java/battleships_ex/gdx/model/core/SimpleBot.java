package battleships_ex.gdx.model.core;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

// A dedicated class to encapsulate the state and logic of the pre-determined computer player.
public class SimpleBot {
    private int currentRow = 0;
    private int currentCol = 0;
    public Coordinate getNextMove() {
        Coordinate target = new Coordinate(currentRow, currentCol);

        currentCol++;
        if (currentCol >= 10) {
            currentCol = 0;
            currentRow++;
        }
        return target;
    }
    public void placeHardcodedShips(Board board) {
        board.placeShip(new Ship(ShipType.CARRIER, Orientation.HORIZONTAL), new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(new Ship(ShipType.CRUISER, Orientation.HORIZONTAL), new Coordinate(1, 0), Orientation.HORIZONTAL);
        board.placeShip(new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL), new Coordinate(2, 0), Orientation.HORIZONTAL);
        board.placeShip(new Ship(ShipType.SUBMARINE, Orientation.HORIZONTAL), new Coordinate(3, 0), Orientation.HORIZONTAL);
        board.placeShip(new Ship(ShipType.PATROL, Orientation.HORIZONTAL), new Coordinate(4, 0), Orientation.HORIZONTAL);
    }
}
