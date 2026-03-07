package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import battleships_ex.gdx.config.ShipCardConfig;

public class ShipCard extends Table {

    private final ShipCardConfig config;

    public ShipCard(ShipCardConfig config) {
        this.config = config;

        setBackground(Theme.darkBluePanel);
        pad(10);

        Image image = new Image(config.sprite);
        Label name = new Label(config.text, new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));

        add(image).growX().height(config.height * 0.45f).padBottom(8).row();
        add(name).center();
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
