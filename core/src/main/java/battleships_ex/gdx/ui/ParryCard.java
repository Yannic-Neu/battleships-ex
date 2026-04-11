package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParryCard extends ActionCardBase {
    public ParryCard(TextureRegion icon) {
        super("PARRY", "Counter an attack.",
            "Reflects an enemy shot back to a random cell on their grid.",
            icon, 1);
    }
}
