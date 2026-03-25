package battleships_ex.gdx.controller;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.GameSession;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;
import battleships_ex.gdx.model.rules.ShotResult;
import battleships_ex.gdx.model.rules.StandardRulesEngine;

public class GameController {

    private final StandardRulesEngine engine;
    private final FirebaseClient       firebase;

    private GameSession  session;
    private Player       localPlayer;
    private Player       remotePlayer;
    private GameListener listener;

    public GameController(StandardRulesEngine engine, FirebaseClient firebase) {
        this.engine   = engine;
        this.firebase = firebase;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public void initSession(Player local, Player remote) {
        this.localPlayer  = local;
        this.remotePlayer = remote;
        this.session      = new GameSession(local, remote);
    }

    public void startGame() {
        requireSession();
        session.startGame();
    }

    public void placeShip(Ship ship, Coordinate start, Orientation orientation) {
        requireSession();

        PlacementResult result = engine.validatePlacement(
            localPlayer.getBoard(), ship, start, orientation);

        if (!result.isValid()) {
            notify_placementRejected(result.getReason());
            return;
        }

        localPlayer.getBoard().placeShip(ship, start, orientation);

        notify_shipPlaced(ship);
    }

    public void playActionCard(battleships_ex.gdx.model.cards.ActionCard card) {
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) return;

        battleships_ex.gdx.model.cards.ActionCardResult result = session.playActionCard(card);

        if (listener != null) listener.onActionCardPlayed(result);
        firebase.pushActionCardEvent(card.getDisplayName(), result.getAffectedCoordinates());

        // Check win condition - some cards (e.g. Airstrike) can sink ships
        if (result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.SUNK
            && engine.hasWon(remotePlayer.getBoard())) {
            notify_gameOver(localPlayer.getName());
            firebase.pushGameOver(localPlayer.getName());
        }
    }

    public void fireShot(int row, int col) {
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) return;

        Coordinate target = new Coordinate(row, col);
        ShotResult result  = engine.resolveShot(remotePlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case ALREADY_SHOT:
                notify_alreadyShot(target);
                return;

            case MISS:
                session.processMove(target);    // records move + switches turn
                notify_miss(target);
                firebase.pushShotEvent(target, false);
                break;

            case HIT:
                session.processMove(target);    // records move; turn stays (hit-again)
                notify_hit(target, result.getSunkShip());
                firebase.pushShotEvent(target, true);
                break;

            case SUNK:
                session.processMove(target);
                notify_sunk(target, result.getSunkShip());
                firebase.pushShotEvent(target, true);

                if (engine.hasWon(remotePlayer.getBoard())) {
                    notify_gameOver(localPlayer.getName());
                    firebase.pushGameOver(localPlayer.getName());
                }
                break;
        }
    }

    public void onRemoteShotReceived(int row, int col) {
        if (!isSessionActive()) return;

        Coordinate target = new Coordinate(row, col);
        ShotResult result  = engine.resolveShot(localPlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case MISS:
                session.processMove(target);    // switches turn back to local
                break;

            case HIT:
            case SUNK:
                session.processMove(target);
                if (result.isSunk() && engine.hasWon(localPlayer.getBoard())) {
                    notify_gameOver(remotePlayer.getName());
                }
                break;

            case ALREADY_SHOT:
                // Stale Firebase event — ignore silently.
                break;
        }
    }

    public boolean isLocalPlayerTurn() {
        return session != null
            && session.isStarted()
            && !session.gameIsOver()
            && session.getCurrentPlayer() == localPlayer;
    }

    /** @return true if a session is active and not yet over */
    public boolean isSessionActive() {
        return session != null && session.isStarted() && !session.gameIsOver();
    }

    /** @return the current GameSession, or null if not yet initialised */
    public GameSession getSession() {
        return session;
    }

    private void notify_miss(Coordinate c) {
        if (listener != null) listener.onMiss(c);
    }

    private void notify_hit(Coordinate c, Ship ship) {
        if (listener != null) listener.onHit(c, ship);
    }

    private void notify_sunk(Coordinate c, Ship ship) {
        if (listener != null) listener.onSunk(c, ship);
    }

    private void notify_gameOver(String winnerName) {
        if (listener != null) listener.onGameOver(winnerName);
    }

    private void notify_alreadyShot(Coordinate c) {
        if (listener != null) listener.onAlreadyShot(c);
    }

    private void notify_shipPlaced(Ship ship) {
        if (listener != null) listener.onShipPlaced(ship);
    }

    private void notify_placementRejected(PlacementResult.Reason reason) {
        if (listener != null) listener.onPlacementRejected(reason);
    }

    private void requireSession() {
        if (session == null) {
            throw new IllegalStateException(
                "GameController: initSession() must be called before this operation.");
        }
    }

    public interface FirebaseClient {

        void pushShotEvent(Coordinate coordinate, boolean hit);

        void pushGameOver(String winnerName);

        void pushActionCardEvent(String cardName,
                                 java.util.List<battleships_ex.gdx.model.board.Coordinate> affectedCoordinates);
    }
}
