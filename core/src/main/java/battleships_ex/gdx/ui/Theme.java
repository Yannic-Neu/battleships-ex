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

    public static BitmapFont fontSmall;
    public static BitmapFont fontMedium;
    public static BitmapFont fontLarge;

    public static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    public static final Color GRAY = new Color(0.6f, 0.6f, 0.6f, 1f);
    public static final Color DARK_BG = new Color(0f, 0f, 0f, 1f);
    public static final Color BLUE = new Color(0f, 0f, 0.3f, 1f);
    public static final Color DARK_BLUE = new Color(0f, 0f, 0.3f, 0.4f);

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

        bluePanel = base.tint(BLUE);
        darkBluePanel = base.tint(DARK_BLUE);
        blackPanel = base.tint(DARK_BG);

        fontSmall = new BitmapFont();

        fontMedium = new BitmapFont();
        fontMedium.getData().setScale(1.2f);

        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(2.0f);
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
