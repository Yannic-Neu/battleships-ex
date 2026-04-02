package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import battleships_ex.gdx.ui.ActionCardBase;

public class ShieldCard extends ActionCardBase {
    public ShieldCard(TextureRegion icon) {
        super(
            "SHIELD",
            "Block one hit.",
            "Absorbs the next incoming attack on your fleet.",
            icon
        );
    }
}
