package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DoubleShotCardPresentation extends ActionCardPresentationBase {
    public DoubleShotCardPresentation(TextureRegion icon) {
        super(
            "DOUBLE SHOT",
            "Fire twice this turn.",
            "Allows two separate shots this turn (not the same cell).",
            icon,
            1);
    }
}
