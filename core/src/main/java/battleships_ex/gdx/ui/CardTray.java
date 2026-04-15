package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CardTray extends Table {
    private final Table cardRow;
    private final ScrollPane scrollPane;

    public CardTray() {
        this.cardRow = new Table();
        this.cardRow.defaults().padRight(5);

        this.scrollPane = new ScrollPane(cardRow);
        this.scrollPane.setFadeScrollBars(false);
        this.scrollPane.setScrollingDisabled(false, true);

        this.add(scrollPane).growX().height(100f);
    }

    /**
     * TODO: Update this to accept ActionCard interface when implemented.
     */
    public void addCard(Actor card) {
        cardRow.add(card);
    }

    public void clearCards() {
        cardRow.clearChildren();
    }
}
