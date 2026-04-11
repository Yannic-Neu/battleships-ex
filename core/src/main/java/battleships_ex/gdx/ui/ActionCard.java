package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class ActionCard extends Table {

    private final GameConfig.ActionCardConfig config;
    private battleships_ex.gdx.model.cards.ActionCard modelCard;


    public void setModelCard(battleships_ex.gdx.model.cards.ActionCard modelCard) {
        this.modelCard = modelCard;
    }

    public battleships_ex.gdx.model.cards.ActionCard getModelCard() {
        return modelCard;
    }


    private ActionCardPresentation model;
    private final Table front = new Table();
    private final Table back  = new Table();
    private boolean showingFront = true;
    private boolean disabled = false;

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

        buildFront();
        buildBack();

        back.setVisible(false);
        addActor(front);
        addActor(back);

        addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                flip();
            }
        });
    }

    public void bind(ActionCardPresentation model) {
        this.model = model;
        refreshBackText();
    }

    private void buildFront() {
        front.setFillParent(true);

        Label name = new Label(config.text, new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        name.setWrap(true);

        front.add(name).center();
    }

    private void buildBack() {
        back.setFillParent(true);
    }

    private void refreshBackText() {
        back.clearChildren();

        Label desc = new Label(
            model != null ? model.getLongText() : config.text,
            new Label.LabelStyle(Theme.fontSmall, Theme.WHITE)
        );
        desc.setWrap(true);

        back.add(desc).width(config.width - 20).center();
    }

    private void flip() {
        showingFront = !showingFront;
        front.setVisible(showingFront);
        back.setVisible(!showingFront);
    }

    @Override
    public float getPrefWidth() { return config.width; }

    @Override
    public float getPrefHeight() { return config.height; }
}
