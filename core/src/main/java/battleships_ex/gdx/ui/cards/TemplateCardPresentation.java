package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TemplateCardPresentation extends ActionCardPresentationBase {
    public TemplateCardPresentation(TextureRegion icon) {
        super(
            "name",
            "Short description of the card",
            "Describe the effect of the card",
            icon,
            1); // number of max uses
    }
}
