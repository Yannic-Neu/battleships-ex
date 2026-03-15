package battleships_ex.gdx.model.core;

import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;

import java.util.ArrayList;
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

    /**
     * Starts the game session. Both players must have placed their ships.
     */
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

    /**
     * Returns the board of the current player's opponent.
     */
    public Board getBoard() {
        return getOpponent().getBoard();
    }

    /**
     * Processes a move: attacks the opponent's board at the given coordinate.
     *
     * @param coordinate the target position
     * @return the resulting Move
     * @throws IllegalStateException if the game has not started or is already over
     */
    public Move processMove(Coordinate coordinate) {
        if (!started) {
            throw new IllegalStateException("Game has not started yet");
        }
        if (gameIsOver()) {
            throw new IllegalStateException("Game is already over");
        }

        Board opponentBoard = getOpponent().getBoard();
        boolean hit = opponentBoard.receiveAttack(coordinate);
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

    /**
     * @return true if one player has sunk all opponent ships
     */
    public boolean gameIsOver() {
        if (!started) return false;
        return player1.getBoard().allShipsSunk() || player2.getBoard().allShipsSunk();
    }

    /**
     * @return the winning player, or null if the game is not over
     */
    public Player getWinner() {
        if (!gameIsOver()) return null;
        return player1.getBoard().allShipsSunk() ? player2 : player1;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    private Player getOpponent() {
        return currentPlayer == player1 ? player2 : player1;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}
