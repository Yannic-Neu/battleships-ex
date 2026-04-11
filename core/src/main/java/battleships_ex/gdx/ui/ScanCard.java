package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ScanCard extends ActionCardBase {
    public ScanCard(TextureRegion icon) {
        super("SCAN", "Reveal area.",
            "Reveals a 3x3 area on the enemy tactical grid.",
            icon, 1);
    }
}
