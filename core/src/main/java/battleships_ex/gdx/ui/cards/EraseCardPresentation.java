package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EraseCardPresentation extends ActionCardPresentationBase {
    public EraseCardPresentation(TextureRegion icon) {
        super(
            "ERASE",
            "Remove a mark.",
            "Erase one revealed miss or hit from your own grid.",
            icon,
            2);
    }
}
