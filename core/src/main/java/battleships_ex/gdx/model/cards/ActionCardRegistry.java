package battleships_ex.gdx.model.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry of all available action cards.
 * Used by the host to select cards and by the GameController to instantiate them.
 */
public class ActionCardRegistry {

    public static class CardMetadata {
        public final String name;
        public final String description;
        public final int energyCost;

        public CardMetadata(String name, String description, int energyCost) {
            this.name = name;
            this.description = description;
            this.energyCost = energyCost;
        }
    }

    private static final List<CardMetadata> ALL_CARDS = new ArrayList<>();

    static {
        ALL_CARDS.add(new CardMetadata("Sonar", "Reveal adjacent ship/mine count", 2));
        ALL_CARDS.add(new CardMetadata("Bomb", "Shoot a 2x2 area", 3));
        ALL_CARDS.add(new CardMetadata("Mine", "Place a defensive mine on your board", 1));
        ALL_CARDS.add(new CardMetadata("Airstrike", "Shoot an entire row or column", 4));
    }

    /**
     * @return a list of metadata for all cards in the pool.
     */
    public static List<CardMetadata> getAllCardMetadata() {
        return Collections.unmodifiableList(ALL_CARDS);
    }

    /**
     * Factory method to create a card instance by its name.
     *
     * @param name the card name
     * @return a new instance of the requested card
     * @throws IllegalArgumentException if the name is unknown
     */
    public static ActionCard createCard(String name) {
        switch (name) {
            case "Sonar":
                return new SonarCard();
            case "Bomb":
                return new BombCard();
            case "Mine":
                return new MineCard();
            case "Airstrike":
                return new AirstrikeCard();
            default:
                throw new IllegalArgumentException("Unknown card: " + name);
        }
    }
}
