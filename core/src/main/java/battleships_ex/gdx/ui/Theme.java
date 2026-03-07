package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Theme {

    private static Texture whiteTexture;

    // Colors
    public static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    public static final Color GRAY = new Color(0.6f, 0.6f, 0.6f, 1f);
    public static final Color BLACK = new Color(0f, 0f, 0f, 1f);

    public static final Color BLUE = new Color(0f, 0f, 0.3f, 1f);
    public static final Color DARK_BLUE = new Color(0f, 0f, 0.2f, 0.6f);

    public static final Color PRIMARY_BUTTON = new Color(0f, 0f, 0.4f, 1f);
    public static final Color SECONDARY_BUTTON = new Color(0f, 0f, 0.1f, 1f);

    // Fonts
    public static BitmapFont fontSmall;
    public static BitmapFont fontMedium;
    public static BitmapFont fontLarge;

    // Shared drawables for common panels
    public static Drawable bluePanel;
    public static Drawable darkBluePanel;
    public static Drawable blackPanel;

    private Theme() {
    }

    public static void init() {
        if (whiteTexture != null) return;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable base =
            new TextureRegionDrawable(new TextureRegion(whiteTexture));

        bluePanel = base.tint(new Color(BLUE));
        darkBluePanel = base.tint(new Color(DARK_BLUE));
        blackPanel = base.tint(new Color(BLACK));

        fontSmall = new BitmapFont();

        fontMedium = new BitmapFont();
        fontMedium.getData().setScale(1.2f);

        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(2.0f);
    }

    public static TextureRegionDrawable whiteDrawable() {
        return new TextureRegionDrawable(new TextureRegion(whiteTexture));
    }

    public static Drawable tintedPanel(Color color) {
        return whiteDrawable().tint(new Color(color));
    }

    public static Color pressedColor(Color color) {
        return new Color(color).mul(0.8f, 0.8f, 0.8f, 1f);
    }

    public static Color hoverColor(Color color) {
        return new Color(
            Math.min(color.r * 1.1f, 1f),
            Math.min(color.g * 1.1f, 1f),
            Math.min(color.b * 1.1f, 1f),
            color.a
        );
    }

    public static void dispose() {
        if (whiteTexture != null) {
            whiteTexture.dispose();
            whiteTexture = null;
        }

        if (fontSmall != null) {
            fontSmall.dispose();
            fontSmall = null;
        }

        if (fontMedium != null) {
            fontMedium.dispose();
            fontMedium = null;
        }

        if (fontLarge != null) {
            fontLarge.dispose();
            fontLarge = null;
        }

        bluePanel = null;
        darkBluePanel = null;
        blackPanel = null;
    }
}
