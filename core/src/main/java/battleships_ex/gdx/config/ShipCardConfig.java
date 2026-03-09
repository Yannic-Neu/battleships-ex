package battleships_ex.gdx.config;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShipCardConfig {
    public final float width;
    public final float height;
    public final String text;
    public final TextureRegion sprite;

    public ShipCardConfig(float width, float height, String text, TextureRegion sprite) {
        this.width = width;
        this.height = height;
        this.text = text;
        this.sprite = sprite;
    }
}
