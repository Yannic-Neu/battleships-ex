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
        public final int maxUses;

        public CardMetadata(String name, String description, int energyCost, int maxUses) {
            this.name = name;
            this.description = description;
            this.energyCost = energyCost;
            this.maxUses = maxUses;
        }
    }

    private static final List<CardMetadata> ALL_CARDS = new ArrayList<>();

    static {
        ALL_CARDS.add(new CardMetadata("Sonar", "Reveal adjacent ship/mine count", 2, 2));
        ALL_CARDS.add(new CardMetadata("Bomb", "Shoot a 2x2 area", 3, 2));
        ALL_CARDS.add(new CardMetadata("Mine", "Place a defensive mine on your board", 1, 3));
        ALL_CARDS.add(new CardMetadata("Airstrike", "Shoot an entire row or column", 4, 1));
    }

    /**
     * @return a list of metadata for all cards in the pool.
     */
    public static List<CardMetadata> getAllCardMetadata() {
        return Collections.unmodifiableList(ALL_CARDS);
    }

    /**
     * Factory method to create a card instance by its name.
     * Note: Card classes will be implemented in Chunk 3.
     * 
     * @param name the card name
     * @return a new instance of the requested card
     * @throws IllegalArgumentException if the name is unknown
     */
    public static ActionCard createCard(String name) {
        // Implementation will be completed as cards are created in Chunk 3.
        // For now, we return null or throw depending on how we want to handle the transition.
        throw new UnsupportedOperationException("Card " + name + " not yet implemented.");
    }
}
