package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;

public class ActionCard extends Table {

    private final GameConfig.ActionCardConfig config;
    private battleships_ex.gdx.model.cards.ActionCard modelCard;
    private ActionCardPresentation model;
    private final Table front = new Table();
    private boolean disabled = false;

    public void setModelCard(battleships_ex.gdx.model.cards.ActionCard modelCard) {
        this.modelCard = modelCard;
    }

    public battleships_ex.gdx.model.cards.ActionCard getModelCard() {
        return modelCard;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        setColor(disabled ? Theme.GRAY : Theme.WHITE);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public ActionCard(GameConfig.ActionCardConfig config) {
        this.config = config;

        setBackground(Theme.tintedPanel(config.color));
        pad(10);
        setTouchable(Touchable.enabled);

        buildFront();
    }

    public void bind(ActionCardPresentation model) {
        this.model = model;
    }

    private void buildFront() {
        front.setFillParent(true);
        front.setTouchable(Touchable.disabled);

        Label name = new Label(config.text, new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        name.setWrap(true);
        name.setAlignment(Align.center);

        front.add(name).width(config.width - 16f).center();
        addActor(front);
    }

    public void showInfoPopup(Stage stage) {
        String description = model != null ? model.getLongText() : config.text;
        new ConfirmationDialog(
            config.text,
            description,
            "OK",
            null,
            null
        ).show(stage);
    }

    @Override
    public float getPrefWidth()  { return config.width; }

    @Override
    public float getPrefHeight() { return config.height; }
}
