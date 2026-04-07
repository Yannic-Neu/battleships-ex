package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TemplateCardPresentation extends ActionCardPresentationBase {
    public TemplateCardPresentation(TextureRegion icon) {
        super(
            "NAME",
            "shortText",
            "longText",
            icon
        );
    }
}
