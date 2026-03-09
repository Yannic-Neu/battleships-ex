package battleships_ex.gdx.config;

import com.badlogic.gdx.graphics.Color;

public class BoardConfig {
    public final float size;
    public final int gridSize;
    public final Color backgroundColor;
    public final Color lineColor;

    public BoardConfig(float size, int gridSize, Color backgroundColor, Color lineColor) {
        this.size = size;
        this.gridSize = gridSize;
        this.backgroundColor = new Color(backgroundColor);
        this.lineColor = new Color(lineColor);
    }
}
