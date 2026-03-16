package battleships_ex.gdx.config;

import battleships_ex.gdx.data.Assets;

public enum ShipType {
    PATROL("Patrol", 2),
    DESTROYER("Destroyer", 3),
    SUBMARINE("Submarine", 4),
    CARRIER("Carrier", 5);

    private final String displayName;
    private final int length;
    ShipType(String displayName, int length) {
        this.displayName = displayName;
        this.length = length;
    }
    public String getDisplayName() {
        return displayName;
    }
    public int getLength() {
        return length;
    }
}
