package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class ActionCardBase implements ActionCardModel {

    protected final String name;
    protected final String shortText;
    protected final String longText;
    protected final TextureRegion icon;

    public ActionCardBase(String name, String shortText, String longText, TextureRegion icon) {
        this.name = name;
        this.shortText = shortText;
        this.longText = longText;
        this.icon = icon;
    }

    @Override public String getName() { return name; }
    @Override public String getShortText() { return shortText; }
    @Override public String getLongText() { return longText; }
    @Override public TextureRegion getIcon() { return icon; }
}
