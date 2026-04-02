package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class ActionCardPresentationBase implements ActionCardPresentation {

    protected final String name;
    protected final String shortText;
    protected final String longText;
    protected final TextureRegion icon;

    public ActionCardPresentationBase(String name, String shortText, String longText, TextureRegion icon) {
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
