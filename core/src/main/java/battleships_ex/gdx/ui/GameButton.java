package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import battleships_ex.gdx.config.ButtonConfig;

public class GameButton extends TextButton {

    private final float prefWidth;
    private final float prefHeight;

    public GameButton(String text, ButtonConfig config, Runnable onClick) {
        super(text, createStyle(config));

        this.prefWidth = config.width;
        this.prefHeight = config.height;

        getLabel().setWrap(false);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isDisabled()) return;
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public void updateStyle(ButtonConfig config) {
        setStyle(createStyle(config));
    }

    private static TextButtonStyle createStyle(ButtonConfig config) {
        TextButtonStyle style = new TextButtonStyle();

        style.up = Theme.tintedPanel(config.backgroundColor);
        style.down = Theme.tintedPanel(Theme.pressedColor(config.backgroundColor));
        style.over = Theme.tintedPanel(Theme.hoverColor(config.backgroundColor));
        style.disabled = Theme.tintedPanel(new com.badlogic.gdx.graphics.Color(0.3f, 0.3f, 0.3f, 1f));

        style.font = config.font;
        style.fontColor = config.textColor;
        style.disabledFontColor = new com.badlogic.gdx.graphics.Color(0.6f, 0.6f, 0.6f, 1f);

        return style;
    }

    @Override
    public float getPrefWidth() {
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        return prefHeight;
    }
}
