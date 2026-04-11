package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ActionCardModel {
    String getName();
    String getShortText();
    String getLongText();
    TextureRegion getIcon();
    int getMaxUses();
    int getRemainingUses();
}
