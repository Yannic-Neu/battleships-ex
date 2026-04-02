package battleships_ex.gdx.ui.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import battleships_ex.gdx.ui.ActionCardBase;

public class EraseCard extends ActionCardBase {
    public EraseCard(TextureRegion icon) {
        super(
            "ERASE",
            "Remove a mark.",
            "Erase one revealed hit or miss from your grid.",
            icon
        );
    }
}
