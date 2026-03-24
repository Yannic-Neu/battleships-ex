package battleships_ex.gdx.model.core;

import battleships_ex.gdx.config.board.AttackResult;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the state of a single game between two players.
 * Tracks turns, moves, and win conditions.
 */
public class GameSession {

    private final Player player1;
    private final Player player2;
    private final List<Move> moveHistory;
    private Player currentPlayer;
    private boolean started;

    public GameSession(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.moveHistory = new ArrayList<>();
        this.currentPlayer = player1;
        this.started = false;
    }

    public void startGame() {
        this.started = true;
        this.currentPlayer = player1;
    }

    public boolean isStarted() {
        return started;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getOpponentBoard() {
        return getOpponent().getBoard();
    }

    public Move processMove(Coordinate coordinate) {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        if (gameIsOver()) {
            throw new IllegalStateException("Game is already over");
        }

        Board opponentBoard = getOpponent().getBoard();
        AttackResult attackResult = opponentBoard.attack(coordinate);

        // Move records true if the shot connected (HIT or SUNK), false for MISS.
        // ALREADY_HIT should be guarded by the RulesEngine before reaching here.
        boolean hit = (attackResult == AttackResult.HIT || attackResult == AttackResult.SUNK);
        Move move = new Move(coordinate, hit);
        moveHistory.add(move);

        if (!hit) {
            switchTurn();
        }

        return move;
    }

    // TODO #31: Implement playActionCard(card) once ActionCard interface exists
    public void playActionCard(Object card) {
        throw new UnsupportedOperationException("Action cards not yet implemented (see #31)");
    }

    public boolean gameIsOver() {
        if (!started) return false;
        return player1.getBoard().allShipsSunk() || player2.getBoard().allShipsSunk();
    }

    public Player getWinner() {
        if (!gameIsOver()) return null;
        return player1.getBoard().allShipsSunk() ? player2 : player1;
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    private Player getOpponent() {
        return currentPlayer == player1 ? player2 : player1;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}
