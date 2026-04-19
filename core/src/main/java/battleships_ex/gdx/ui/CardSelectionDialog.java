package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.model.cards.ActionCardRegistry;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;
import battleships_ex.gdx.ui.cards.ActionCardPresentationBase;

public class CardSelectionDialog extends Dialog {

    public interface SelectionCallback {
        void onCardsSelected(List<String> cardNames);
    }

    private final SelectionCallback callback;
    private final List<String> selectedNames = new ArrayList<>();
    private final List<ActionCard> cardActors = new ArrayList<>();
    private final Label counterLabel;
    private final GameButton confirmButton;

    public CardSelectionDialog(SelectionCallback callback) {
        super("TACTICAL DECK SELECTION", Theme.dialogStyle);
        this.callback = callback;

        pad(20);
        
        getContentTable().defaults().pad(10);
        
        Label instruction = new Label("CHOOSE EXACTLY 4 ABILITIES FOR THIS MISSION", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        instruction.setAlignment(Align.center);
        getContentTable().add(instruction).colspan(2).row();

        Table grid = new Table();
        grid.defaults().pad(10);
        
        List<ActionCardRegistry.CardMetadata> allMeta = ActionCardRegistry.getAllCardMetadata();
        int cols = 2;
        for (int i = 0; i < allMeta.size(); i++) {
            ActionCardRegistry.CardMetadata meta = allMeta.get(i);
            
            ActionCard actor = new ActionCard(new GameConfig.ActionCardConfig(160f, 100f, true, Theme.BLUE, meta.name));
            ActionCardPresentation pres = new ActionCardPresentationBase(meta.name, "", meta.description, null, meta.maxUses);
            actor.bind(pres);
            
            actor.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (getTapCount() >= 2) {
                        actor.showInfoPopup(getStage());
                    } else {
                        toggleSelection(meta.name, actor);
                    }
                }
            });
            
            grid.add(actor).size(160f, 100f);
            cardActors.add(actor);
            if ((i + 1) % cols == 0) grid.row();
        }
        
        getContentTable().add(grid).row();

        counterLabel = new Label("0 / 4 SELECTED", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        getContentTable().add(counterLabel).padTop(10).row();

        confirmButton = new GameButton("CONFIRM DECK", battleships_ex.gdx.config.ButtonConfig.primary(240f, 60f), () -> {
            if (selectedNames.size() == 4) {
                callback.onCardsSelected(new ArrayList<>(selectedNames));
                hide();
            }
        });
        confirmButton.setDisabled(true);
        getButtonTable().add(confirmButton).padBottom(20);
    }

    private void toggleSelection(String name, ActionCard actor) {
        if (selectedNames.contains(name)) {
            selectedNames.remove(name);
            actor.setSelected(false);
        } else if (selectedNames.size() < 4) {
            selectedNames.add(name);
            actor.setSelected(true);
        }
        
        updateUI();
    }

    private void updateUI() {
        counterLabel.setText(selectedNames.size() + " / 4 SELECTED");
        confirmButton.setDisabled(selectedNames.size() != 4);
        
        if (selectedNames.size() == 4) {
            counterLabel.setColor(Theme.YELLOW);
        } else {
            counterLabel.setColor(Theme.WHITE);
        }
    }

    @Override
    public Dialog show(Stage stage) {
        return super.show(stage);
    }
}
