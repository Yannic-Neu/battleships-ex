package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ScanCardPresentation extends ActionCardPresentationBase {
    public ScanCardPresentation(TextureRegion icon) {
        super(
            "SCAN",
            "Reveal area.",
            "Reveals a 3x3 area on the enemy tactical grid.",
            icon,
            1);
    }
}
