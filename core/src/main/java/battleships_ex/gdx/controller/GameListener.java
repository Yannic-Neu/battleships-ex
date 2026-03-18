package battleships_ex.gdx.controller;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;

public interface GameListener {

    void onMiss(Coordinate coordinate);
    void onHit(Coordinate coordinate, Ship ship);
    void onSunk(Coordinate coordinate, Ship ship);
    void onGameOver(String winnerName);
    void onAlreadyShot(Coordinate coordinate);
    void onShipPlaced(Ship ship);
    void onPlacementRejected(battleships_ex.gdx.model.rules.PlacementResult.Reason reason);
}
