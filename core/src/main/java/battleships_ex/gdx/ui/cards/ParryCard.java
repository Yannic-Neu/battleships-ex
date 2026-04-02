package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import battleships_ex.gdx.ui.ActionCardBase;

public class ParryCard extends ActionCardBase {
    public ParryCard(TextureRegion icon) {
        super(
            "PARRY",
            "Counter an attack.",
            "Reflect an enemy shot back to a random cell on their grid.",
            icon
        );
    }
}
