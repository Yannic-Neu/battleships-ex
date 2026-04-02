package battleships_ex.gdx.config;

import com.badlogic.gdx.graphics.Color;

public final class GameConfig {
    private GameConfig() {}

    public static final float WORLD_WIDTH = 450f;
    public static final float WORLD_HEIGHT = 800f;

    public static class ActionCardConfig {

        public final float width;
        public final float height;

        public final boolean playable;
        public final Color color;
        public final String text;

        public ActionCardConfig(float width, float height, boolean playable, Color color, String text) {
            this.width = width;
            this.height = height;
            this.playable = playable;
            this.color = color;
            this.text = text;
        }
    }

    public static class BoardConfig {

        public final float size;
        public final int gridSize;
        public final Color backgroundColor;
        public final Color gridColor;

        public BoardConfig(float size, int gridSize, Color backgroundColor, Color gridColor) {
            this.size = size;
            this.gridSize = gridSize;
            this.backgroundColor = backgroundColor;
            this.gridColor = gridColor;
        }
    }
}
