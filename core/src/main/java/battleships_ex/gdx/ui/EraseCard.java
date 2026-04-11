package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EraseCard extends ActionCardBase {
    public EraseCard(TextureRegion icon) {
        super("ERASE", "Remove a mark.",
            "Erase one revealed miss or hit from your own grid.",
            icon, 2);
    }
}
