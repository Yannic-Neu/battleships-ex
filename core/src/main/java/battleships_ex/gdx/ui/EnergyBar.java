package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class EnergyBar extends Table {

    private final Label energyLabel;

    public EnergyBar() {
        setBackground(Theme.darkBluePanel);
        pad(8);

        energyLabel = new Label(
            "ENERGY: 0",
            new Label.LabelStyle(Theme.fontSmall, Theme.WHITE)
        );

        add(energyLabel);
    }

    /**
     * Updates the displayed energy value.
     */
    public void updateEnergy(int energy) {
        energyLabel.setText("ENERGY: " + energy);
    }
}
