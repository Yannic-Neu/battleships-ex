package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DoubleShotCard extends ActionCardBase {
    public DoubleShotCard(TextureRegion icon) {
        super("DOUBLE SHOT", "Fire twice this turn.",
            "Allows two separate shots this turn (not the same cell).",
            icon, 1);
    }
}
