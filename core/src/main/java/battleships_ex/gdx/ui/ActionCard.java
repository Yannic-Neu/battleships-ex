package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;

public class ActionCard extends Table {

    private final GameConfig.ActionCardConfig config;
    private battleships_ex.gdx.model.cards.ActionCard modelCard;
    private ActionCardPresentation model;
    private final Table front = new Table();
    private boolean disabled = false;

    private float holdTimer = 0f;
    private boolean isHolding = false;
    private boolean popupVisible = false;
    private boolean wasLongPressed = false;
    private static final float HOLD_TIME = 0.5f;

    private final Table holdProgressBarBg;
    private final Table holdProgressBarFg;
    private Table infoPopup;

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

    public void setSelected(boolean selected) {
        if (selected) {
            setBackground(Theme.tintedPanel(Theme.YELLOW));
        } else {
            setBackground(Theme.tintedPanel(config.color));
        }
    }



    public boolean wasLongPressed() {
        return wasLongPressed;
    }

    public ActionCard(GameConfig.ActionCardConfig config) {
        this.config = config;

        setBackground(Theme.tintedPanel(config.color));
        pad(6);
        setTouchable(Touchable.enabled);

        buildFront();

        holdProgressBarBg = new Table();
        holdProgressBarBg.setBackground(Theme.tintedPanel(Color.DARK_GRAY));
        holdProgressBarBg.setVisible(false);
        holdProgressBarBg.setSize(config.width - 12f, 6f);

        holdProgressBarFg = new Table();
        holdProgressBarFg.setBackground(Theme.tintedPanel(Theme.YELLOW));
        holdProgressBarBg.addActor(holdProgressBarFg);
        holdProgressBarFg.setHeight(6f);

        addActor(holdProgressBarBg);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                isHolding = true;
                holdTimer = 0f;
                wasLongPressed = false;

                holdProgressBarBg.setVisible(true);
                holdProgressBarFg.setWidth(0);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isHolding = false;
                holdProgressBarBg.setVisible(false);
                if (popupVisible) {
                    hidePopup();
                }
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        holdProgressBarBg.setPosition(6f, 6f);

        if (isHolding) {
            holdTimer += delta;
            float progress = Math.min(holdTimer / HOLD_TIME, 1.0f);
            holdProgressBarFg.setWidth((config.width - 12f) * progress);

            if (holdTimer >= HOLD_TIME) {
                wasLongPressed = true;
                if (!popupVisible) {
                    showPopup();
                }
            }
        }
    }

    public void bind(ActionCardPresentation model) {
        this.model = model;
        front.clearChildren();

        if (model.getIcon() != null) {
            front.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(model.getIcon())).size(32, 32).padBottom(4).row();
        }

        Label nameLabel = new Label(model.getName(), new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        nameLabel.setWrap(true);
        nameLabel.setAlignment(Align.center);
        front.add(nameLabel).width(config.width - 12f).center().row();

        if (modelCard != null) {
            Label costLabel = new Label("E: " + modelCard.getEnergyCost(), new Label.LabelStyle(Theme.fontSmall, Theme.YELLOW));
            front.add(costLabel).padTop(4).center();
        }
    }

    private void buildFront() {
        front.setFillParent(true);
        front.setTouchable(Touchable.disabled);
        addActor(front);

        Label name = new Label(config.text, new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        name.setWrap(true);
        name.setAlignment(Align.center);
        front.add(name).width(config.width - 16f).center();
    }

    private void showPopup() {
        if (getStage() == null) return;
        popupVisible = true;

        if (infoPopup == null) {
            infoPopup = new Table();
            infoPopup.setBackground(Theme.tintedPanel(new Color(0.1f, 0.1f, 0.15f, 0.95f)));
            infoPopup.pad(15);
            infoPopup.setTouchable(Touchable.disabled);
        }

        infoPopup.clearChildren();
        Label titleLabel = new Label(config.text, new Label.LabelStyle(Theme.fontMedium, Theme.YELLOW));
        String description = model != null ? model.getLongText() : config.text;
        Label descLabel = new Label(description, new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        descLabel.setWrap(true);
        infoPopup.add(titleLabel).center().padBottom(8).row();
        infoPopup.add(descLabel).width(250).center();
        infoPopup.pack();

        Vector2 pos = localToStageCoordinates(new Vector2(0, getHeight()));
        infoPopup.setPosition(pos.x + getWidth() / 2f - infoPopup.getWidth() / 2f, pos.y + 10f);

        if (infoPopup.getX() < 10) infoPopup.setX(10);
        if (infoPopup.getRight() > getStage().getWidth() - 10) infoPopup.setX(getStage().getWidth() - infoPopup.getWidth() - 10);
        if (infoPopup.getTop() > getStage().getHeight() - 10) {
            Vector2 bottomPos = localToStageCoordinates(new Vector2(0, 0));
            infoPopup.setY(bottomPos.y - infoPopup.getHeight() - 10f);
        }

        infoPopup.getColor().a = 0f;
        infoPopup.addAction(Actions.fadeIn(0.15f));
        getStage().addActor(infoPopup);
    }

    private void hidePopup() {
        popupVisible = false;
        if (infoPopup != null && infoPopup.getParent() != null) {
            infoPopup.addAction(Actions.sequence(
                Actions.fadeOut(0.1f),
                Actions.removeActor()
            ));
        }
    }

    @Override
    public float getPrefWidth()  { return config.width; }

    @Override
    public float getPrefHeight() { return config.height; }
}
