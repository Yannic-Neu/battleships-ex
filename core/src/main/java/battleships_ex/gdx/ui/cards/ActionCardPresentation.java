package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ActionCardPresentation {
    String getName();
    String getShortText();
    String getLongText();
    TextureRegion getIcon();
}
