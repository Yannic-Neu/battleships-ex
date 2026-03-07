package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public final class ButtonConfig {
    public final float width;
    public final float height;
    public final float radius;
    public final Color backgroundColor;
    public final Color textColor;
    public final BitmapFont font;

    public ButtonConfig(
        float width,
        float height,
        float radius,
        Color backgroundColor,
        Color textColor,
        BitmapFont font
    ) {
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.backgroundColor = new Color(backgroundColor);
        this.textColor = new Color(textColor);
        this.font = font;
    }

    public static ButtonConfig primary(float width, float height) {
        return new ButtonConfig(
            width,
            height,
            16f,
            Theme.PRIMARY_BUTTON,
            Theme.WHITE,
            Theme.fontMedium
        );
    }

    public static ButtonConfig secondary(float width, float height) {
        return new ButtonConfig(
            width,
            height,
            16f,
            Theme.SECONDARY_BUTTON,
            Theme.WHITE,
            Theme.fontMedium
        );
    }
}
