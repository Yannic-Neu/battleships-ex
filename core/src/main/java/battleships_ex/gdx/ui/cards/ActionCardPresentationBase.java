package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class ActionCardPresentationBase implements ActionCardPresentation {

    protected final String name, shortText, longText;
    protected final TextureRegion icon;
    protected final int maxUses;
    protected int used = 0;

    protected ActionCardPresentationBase(String name, String shortText, String longText,
                             TextureRegion icon, int maxUses) {
        this.name = name; this.shortText = shortText; this.longText = longText;
        this.icon = icon; this.maxUses = maxUses;
    }

    @Override public String getName()        { return name; }
    @Override public String getShortText()   { return shortText; }
    @Override public String getLongText()    { return longText; }
    @Override public TextureRegion getIcon() { return icon; }
    @Override public int getMaxUses()        { return maxUses; }
    @Override public int getRemainingUses()  { return Math.max(0, maxUses - used); }

    public void markUsed() { used = Math.min(maxUses, used + 1); }
}
