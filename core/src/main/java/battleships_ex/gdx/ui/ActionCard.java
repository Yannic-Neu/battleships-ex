package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import battleships_ex.gdx.config.GameConfig;

public class ActionCard extends Table {

    private final GameConfig.ActionCardConfig config;

    public ActionCard(GameConfig.ActionCardConfig config) {
        this.config = config;

        setBackground(Theme.tintedPanel(config.color));
        pad(12);

        Label title = new Label(
            config.text,
            new Label.LabelStyle(Theme.fontMedium, Theme.WHITE)
        );

        title.setWrap(true);

        add(title).grow().center();
    }

    @Override
    public float getPrefWidth() {
        return config.width;
    }

    @Override
    public float getPrefHeight() {
        return config.height;
    }
}
