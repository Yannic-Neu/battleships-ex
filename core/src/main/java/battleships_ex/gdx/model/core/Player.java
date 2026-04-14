package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.cards.ActionCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Set;
import java.util.HashSet;

import battleships_ex.gdx.model.board.Coordinate;


/**
 * Represents a player in the game, owning a board and a hand of action cards.
 */
public class Player {

    private final String id;
    private final String name;
    private final Board  board;
    private final List<ActionCard> cards;
    private int energy;
    private final Set<Coordinate> shieldedTiles = new HashSet<>();
    public void addShieldedTile(Coordinate coord) {
        shieldedTiles.add(coord);
    }

    public boolean isTileShielded(Coordinate coord) {
        return shieldedTiles.contains(coord);
    }

    public void removeShieldedTile(Coordinate coord) {
        shieldedTiles.remove(coord);
    }


    private Coordinate pendingTarget;

    public void setPendingTarget(Coordinate coord) {
        this.pendingTarget = coord;
    }

    public Coordinate getPendingTarget() {
        return pendingTarget;
    }

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
        for (ActionCard existing : cards) {
            if (existing.getClass() == card.getClass()) {
                return;
            }
        }
        cards.add(card);
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
    public void gainTurnEnergy() {
        addEnergy(1);
    }

    public void setParryActive(boolean b) {
    }
}
