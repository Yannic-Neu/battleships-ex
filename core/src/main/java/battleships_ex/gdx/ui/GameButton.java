package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

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
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    private static TextButtonStyle createStyle(ButtonConfig config) {
        TextButtonStyle style = new TextButtonStyle();

        style.up = Theme.tintedPanel(config.backgroundColor);
        style.down = Theme.tintedPanel(Theme.pressedColor(config.backgroundColor));
        style.over = Theme.tintedPanel(Theme.hoverColor(config.backgroundColor));

        style.font = config.font;
        style.fontColor = config.textColor;

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
