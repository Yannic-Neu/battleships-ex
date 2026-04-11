package battleships_ex.gdx.model.core;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.cards.ActionCard;
import battleships_ex.gdx.model.cards.ActionCardResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the state of a single game between two players.
 * Tracks turns, moves, and win conditions.
 */
public class GameSession {

    private final Player       player1;
    private final Player       player2;
    private final List<Move>   moveHistory;
    private Player  currentPlayer;
    private boolean started;

    public GameSession(Player player1, Player player2) {
        this.player1       = player1;
        this.player2       = player2;
        this.moveHistory   = new ArrayList<>();
        this.currentPlayer = player1;
        this.started       = false;
    }

    public void startGame() {
        this.started       = true;
        this.currentPlayer = player1;
    }

    public boolean isStarted()           { return started; }
    public Player  getCurrentPlayer()    { return currentPlayer; }
    public Board   getOpponentBoard()    { return getOpponent().getBoard(); }
    public Player  getPlayer1()          { return player1; }
    public Player  getPlayer2()          { return player2; }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    // -------------------------------------------------------------------------
    // Move processing
    // -------------------------------------------------------------------------

    /**
     * Processes a shot at the given coordinate on the opponent's board.
     *
     * Delegates to {@link Board#attack(Coordinate)} → {@link AttackResult}.
     * Turn switches only on a MISS; HIT and SUNK keep the turn with the attacker.
     * ALREADY_HIT is guarded by the RulesEngine before reaching here.
     */
    public Move processMove(Coordinate coordinate) {
        requireStarted();
        requireNotOver();

        Board        opponentBoard = getOpponent().getBoard();
        AttackResult attackResult  = opponentBoard.attack(coordinate);

        boolean hit = (attackResult == AttackResult.HIT || attackResult == AttackResult.SUNK);
        Move move   = new Move(coordinate, hit);
        moveHistory.add(move);

        if (!hit) switchTurn();

        return move;
    }

    // -------------------------------------------------------------------------
    // Action card
    // -------------------------------------------------------------------------

    /**
     * Plays an action card for the current player against the opponent.
     *
     * Guards:
     *   - Session must be started and not over.
     *   - The card must be in the current player's hand.
     *   - {@link ActionCard#canUse} must return true.
     *
     * On success: executes the card, removes it from the player's hand,
     * and returns the result for GameController to broadcast.
     *
     * Action cards do not switch turns — playing a card is in addition to
     * the player's normal shot, not instead of it.
     *
     * @param card the card to play
     * @return an {@link ActionCardResult} describing the effect
     * @throws IllegalArgumentException if the card is null or not in hand
     * @throws IllegalStateException    if canUse() returns false
     */
    public ActionCardResult playActionCard(ActionCard card) {
        requireStarted();
        requireNotOver();

        if (card == null) {
            throw new IllegalArgumentException("Card must not be null");
        }

        Player user     = currentPlayer;
        Player opponent = getOpponent();

        if (!user.hasCard(card)) {
            throw new IllegalArgumentException(
                "Player " + user.getName() + " does not hold card: " + card.getClass().getSimpleName());
        }

        if (!card.canUse(user, opponent)) {
            throw new IllegalStateException(
                "Card cannot be used right now: " + card.getClass().getSimpleName());
        }

        ActionCardResult result = card.execute(user, opponent);
        user.removeCard(card);   // one-time use — remove after execution
        return result;
    }

    // -------------------------------------------------------------------------
    // Win condition
    // -------------------------------------------------------------------------

    public boolean gameIsOver() {
        if (!started) return false;
        return player1.getBoard().allShipsSunk() || player2.getBoard().allShipsSunk();
    }

    public Player getWinner() {
        if (!gameIsOver()) return null;
        return player1.getBoard().allShipsSunk() ? player2 : player1;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Player getOpponent() {
        return currentPlayer == player1 ? player2 : player1;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void requireStarted() {
        if (!started) throw new IllegalStateException("Game has not started yet");
    }

    private void requireNotOver() {
        if (gameIsOver()) throw new IllegalStateException("Game is already over");
    }
    public Player getLocalPlayer() {
        return player1;
    }

    public Player getRemotePlayer() {
        return player2;
    }

    public void setCurrentPlayer(Player localPlayer) {
    }
}
