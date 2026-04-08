package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShieldCardPresentation extends ActionCardPresentationBase {
    public ShieldCardPresentation(TextureRegion icon) {
        super(
            "SHIELD",
            "Block one hit.",
            "Absorbs the next incoming attack on your fleet.",
            icon
        );
    }
}
