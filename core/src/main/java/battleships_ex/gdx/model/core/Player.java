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
    private int energy;
    private boolean shieldActive = false;
    private final java.util.Set<String> cardsPlayedThisTurn = new java.util.HashSet<>();
    private boolean canFireThisTurn = true;

    public void markCardAsPlayed(ActionCard card) {
        cardsPlayedThisTurn.add(card.getClass().getSimpleName());
    }

    public boolean hasPlayedCardThisTurn(ActionCard card) {
        return cardsPlayedThisTurn.contains(card.getClass().getSimpleName());
    }

    public void clearTurnFlags() {
        cardsPlayedThisTurn.clear();
        canFireThisTurn = true;
    }

    public void setCanFireThisTurn(boolean canFire) {
        this.canFireThisTurn = canFire;
    }

    public boolean canFireThisTurn() {
        return canFireThisTurn;
    }

    public void activateShield() {
        shieldActive = true;
    }

    public boolean hasShield() {
        return shieldActive;
    }

    public void consumeShield() {
        shieldActive = false;
    }

    public Player(String id, String name) {
        this.id    = id;
        this.name  = name;
        this.board = new Board(10, 10);
        this.cards = new ArrayList<>();
        this.energy = 0;
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

    public void clearCards() {
        cards.clear();
    }

    public void removeCard(ActionCard card) {
        cards.remove(card);
    }

    /** @return true if the player currently holds the given card */
    public boolean hasCard(ActionCard card) {
        return cards.contains(card);
    }

    public int getEnergy() {
        return energy;
    }

    public void addEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Energy amount must be non-negative");
        }
        energy += amount;
        if (energy > 10) {
            energy = 10;
        }
    }

    public boolean canSpendEnergy(int cost) {
        return energy >= cost;
    }

    public void spendEnergy(int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Energy cost must be non-negative");
        }
        if (!canSpendEnergy(cost)) {
            throw new IllegalStateException("Not enough energy");
        }
        energy -= cost;
    }

    public void setParryActive(boolean b) {
    }
}
