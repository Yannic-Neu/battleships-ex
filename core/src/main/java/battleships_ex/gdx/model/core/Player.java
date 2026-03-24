package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player in the game, owning a board and a hand of action cards.
 */
public class Player {

    private final String id;
    private final String name;
    private final Board board;

    // TODO #31: Replace Object with ActionCard interface once implemented
    private final List<Object> cards;

    public Player(String id, String name) {
        this.id    = id;
        this.name  = name;
        this.board = new Board(10, 10);
        this.cards = new ArrayList<>();
    }

    public String getId()   { return id; }
    public String getName() { return name; }
    public Board  getBoard(){ return board; }

    public List<Object> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void addCard(Object card)    { cards.add(card); }
    public void removeCard(Object card) { cards.remove(card); }
}
