package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EraseCardPresentation extends ActionCardPresentationBase {
    public EraseCardPresentation(TextureRegion icon) {
        super(
            "ERASE",
            "Remove a mark.",
            "Erase one revealed hit or miss from your grid.",
            icon
        );
    }
}
