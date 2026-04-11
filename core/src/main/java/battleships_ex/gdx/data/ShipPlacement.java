package battleships_ex.gdx.data;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

// A simple data-transfer-object for serializing ship data to/from Firebase.
public class ShipPlacement {
    public String type;
    public int row;
    public int col;
    public String orientation;

    // Default constructor for Firebase
    public ShipPlacement() {}

    public ShipPlacement(ShipType type, int row, int col, Orientation orientation) {
        this.type = type.name();
        this.row = row;
        this.col = col;
        this.orientation = orientation.name();
    }
}
