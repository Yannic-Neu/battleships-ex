package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShieldCard extends ActionCardBase {
    public ShieldCard(TextureRegion icon) {
        super("SHIELD", "Block one hit.",
            "Absorbs the next incoming attack on your fleet.",
            icon, 1);
    }
}
