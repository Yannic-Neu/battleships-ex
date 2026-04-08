package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParryCardPresentation extends ActionCardPresentationBase {
    public ParryCardPresentation(TextureRegion icon) {
        super(
            "PARRY",
            "Counter an attack.",
            "Reflect an enemy shot back to a random cell on their grid.",
            icon
        );
    }
}
