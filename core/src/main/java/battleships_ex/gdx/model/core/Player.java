package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.cards.ActionCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player in the game, owning a board and a hand of action cards.
 */
public class Player {

    private final String id;
    private final String name;
    private final Board  board;
    private final List<ActionCard> cards;

    public Player(String id, String name) {
        this.id    = id;
        this.name  = name;
        this.board = new Board(10, 10);
        this.cards = new ArrayList<>();
    }

    public String getId()    { return id; }
    public String getName()  { return name; }
    public Board  getBoard() { return board; }

    public List<ActionCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void addCard(ActionCard card) {
        if (card == null) throw new IllegalArgumentException("Card must not be null");
        cards.add(card);
    }

    public void removeCard(ActionCard card) {
        cards.remove(card);
    }

    /** @return true if the player currently holds the given card */
    public boolean hasCard(ActionCard card) {
        return cards.contains(card);
    }
}
