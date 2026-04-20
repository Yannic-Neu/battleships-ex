package battleships_ex.gdx.controller;

import java.util.List;

import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.SessionManager;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.core.GameSession;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.PlacementResult;
import battleships_ex.gdx.model.rules.ShotResult;
import battleships_ex.gdx.model.rules.StandardRulesEngine;
import battleships_ex.gdx.model.core.SimpleBot;

/**
 * Orchestrates game flow: ship placement, shot resolution, action cards,
 * and real-time synchronization via {@link GameDataSource}.
 *
 * <p>Replaces the former inner {@code FirebaseClient} interface with the
 * platform-agnostic {@link GameDataSource} abstraction (Issue #27).</p>
 */
public class GameController {

    private final StandardRulesEngine engine;
    private final GameDataSource      gameDataSource;
    private final SessionManager      sessionManager;

    /** Minimum interval between preview updates (debounce). */
    private static final long PREVIEW_DEBOUNCE_MS = 200L;

    private GameSession  session;
    private Player       localPlayer;
    private Player       remotePlayer;
    private GameListener listener;
    private String       roomCode;

    /** Timestamp of the last preview update sent. */
    private long lastPreviewSentMs;
    /** The last preview coordinate sent (to avoid duplicate sends). */
    private Coordinate lastPreviewCoord;
    private boolean isSinglePlayer = false;
    private SimpleBot botLogic;

    public GameController(StandardRulesEngine engine,
                          GameDataSource gameDataSource,
                          SessionManager sessionManager) {
        this.engine         = engine;
        this.gameDataSource = gameDataSource;
        this.sessionManager = sessionManager;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    /**
     * Initialises the game session and wires up real-time listeners.
     *
     * @param local    the local player
     * @param remote   the remote player
     * @param roomCode the active room code for Firebase sync
     */
    public void initSession(Player local, Player remote, String roomCode, boolean localIsPlayer1, java.util.List<String> selectedCardNames) {
        System.out.println("[GameController] LOG: Initializing session. localIsPlayer1: " + localIsPlayer1);
        this.localPlayer  = local;
        this.remotePlayer = remote;
        this.roomCode     = roomCode;

        if (localIsPlayer1) {
            this.session = new battleships_ex.gdx.model.core.GameSession(local, remote);
        } else {
            this.session = new battleships_ex.gdx.model.core.GameSession(remote, local);
        }

        // Initialize action cards if provided
        if (selectedCardNames != null && !selectedCardNames.isEmpty()) {
            local.clearCards(); // Clear any default cards
            for (String cardName : selectedCardNames) {
                try {
                    local.addCard(battleships_ex.gdx.model.cards.ActionCardRegistry.createCard(cardName));
                } catch (UnsupportedOperationException e) {
                    System.err.println("[GameController] Card not yet implemented: " + cardName);
                }
            }
        }
    }

    /**
     * Legacy/Backward-compatible overload
     */
    public void initSession(Player local, Player remote, String roomCode, boolean localIsPlayer1) {
        initSession(local, remote, roomCode, localIsPlayer1, java.util.Collections.emptyList());
    }

    /**
     * Backward-compatible overload for local/stub usage without room code.
     */
    public void initSession(Player local, Player remote) {
        initSession(local, remote, null, true);
    }

    /**
     * Starts the game and activates real-time synchronization.
     */
    public Player getRemotePlayer() {
        return remotePlayer;
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public void startGame() {
        requireSession();
        session.startGame();

        if (roomCode != null) {
            // Update game status to "playing"
            gameDataSource.updateGameStatus(roomCode, "playing", new DataCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Failed to update game status: " + error);
                }
            });

            // Sync initial turn
            System.out.println("[GameController] LOG: Syncing initial turn to: " + session.getCurrentPlayer().getId());
            gameDataSource.syncTurn(roomCode, session.getCurrentPlayer().getId(), new DataCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Failed to sync initial turn: " + error);
                }
            });

            // Register turn listener
            gameDataSource.addTurnListener(roomCode, new DataCallback<String>() {
                @Override
                public void onSuccess(String currentPlayerId) {
                    System.out.println("[GameController] LOG: Turn listener received new turn: " + currentPlayerId);
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if (session.getCurrentPlayer().getId().equals(currentPlayerId)) return;

                        if (currentPlayerId.equals(localPlayer.getId())) {
                            session.setCurrentPlayer(localPlayer);
                            if (listener != null) listener.onTurnChanged(localPlayer.getId());
                        } else {
                            session.setCurrentPlayer(remotePlayer);
                            if (listener != null) listener.onTurnChanged(remotePlayer.getId());
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Turn listener error: " + error);
                }
            });

            // Register move listener for opponent moves
            gameDataSource.addMoveListener(roomCode, new DataCallback<GameDataSource.MoveSnapshot>() {
                @Override
                public void onSuccess(GameDataSource.MoveSnapshot move) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        // Only process moves from the opponent
                        if (!move.playerId.equals(localPlayer.getId())) {
                            onRemoteShotReceived(move.row, move.col);
                            if (listener != null) {
                                listener.onOpponentMoveReceived(move.row, move.col);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Move listener error: " + error);
                }
            });

            // Register action card listener
            gameDataSource.addActionCardListener(roomCode, new DataCallback<GameDataSource.ActionCardSnapshot>() {
                @Override
                public void onSuccess(GameDataSource.ActionCardSnapshot snapshot) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if (snapshot.playerId.equals(localPlayer.getId())) return;
                        onActionCardReceived(snapshot);
                    });
                }
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Action card listener error: " + error);
                }
            });

            // Start session manager (heartbeat + disconnect detection)
            sessionManager.setListener(new SessionManager.SessionListener() {
                @Override
                public void onOpponentDisconnected() {
                    if (listener != null) listener.onOpponentDisconnected();
                }

                @Override
                public void onOpponentReconnected() {
                    if (listener != null) listener.onOpponentReconnected();
                }

                @Override
                public void onSessionTimeout() {
                    if (listener != null) listener.onSessionTimeout();
                }
            });

            sessionManager.startSession(roomCode, localPlayer.getId(), remotePlayer.getId());

            // Register placement status listener to see if opponent is ready
            gameDataSource.addPlacementStatusListener(roomCode, remotePlayer.getId(), new DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean isReady) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if (listener != null) {
                            listener.onOpponentPlacementReady(isReady);
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Placement status listener error: " + error);
                }
            });

            // Register preview listener to see opponent's aim (Issue #28)
            gameDataSource.addPreviewListener(roomCode, remotePlayer.getId(), new DataCallback<Coordinate>() {
                @Override
                public void onSuccess(Coordinate previewCoord) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if (listener != null) {
                            listener.onPreviewReceived(previewCoord);
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Preview listener error: " + error);
                }
            });

            // Register board layout listener
            gameDataSource.addBoardLayoutListener(roomCode, remotePlayer.getId(), new DataCallback<java.util.List<battleships_ex.gdx.data.ShipPlacement>>() {
                @Override
                public void onSuccess(java.util.List<battleships_ex.gdx.data.ShipPlacement> result) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        for (battleships_ex.gdx.data.ShipPlacement placement : result) {
                            remotePlayer.getBoard().placeShip(new Ship(battleships_ex.gdx.config.board.ShipType.valueOf(placement.type), battleships_ex.gdx.config.board.Orientation.valueOf(placement.orientation)), new Coordinate(placement.row, placement.col), battleships_ex.gdx.config.board.Orientation.valueOf(placement.orientation));
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Board layout listener error: " + error);
                }
            });
        }
    }

    /**
     * Sends a target preview to the opponent (Issue #28).
     * The preview is debounced to avoid flooding Firebase with updates.
     *
     * @param target the coordinate being aimed at
     */
    public void updatePreview(Coordinate target) {
        if (!isSessionActive() || !isLocalPlayerTurn()) return;
        if (roomCode == null) return;

        long now = System.currentTimeMillis();

        // Skip if same coordinate or within debounce window
        if (target.equals(lastPreviewCoord) && (now - lastPreviewSentMs) < PREVIEW_DEBOUNCE_MS) {
            return;
        }

        lastPreviewCoord = target;
        lastPreviewSentMs = now;
        gameDataSource.sendPreview(roomCode, localPlayer.getId(), target);
    }

    /**
     * Clears the target preview after a shot is confirmed.
     */
    private void clearPreview() {
        if (roomCode == null) return;
        lastPreviewCoord = null;
        gameDataSource.clearPreview(roomCode, localPlayer.getId());
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

    public void confirmPlacement() {
        requireSession();
        if (roomCode != null) {
            // Upload the board layout
            java.util.List<battleships_ex.gdx.data.ShipPlacement> placements = new java.util.ArrayList<>();
            for (Ship ship : localPlayer.getBoard().getShips()) {
                if (ship.isPlaced()) {
                    // Convert Set to List to get the first coordinate, which represents the start
                    Coordinate startCoord = new java.util.ArrayList<>(ship.getOccupiedCoordinates()).get(0);
                    placements.add(new battleships_ex.gdx.data.ShipPlacement(ship.getType(), startCoord.getRow(), startCoord.getCol(), ship.getOrientation()));
                }
            }
            gameDataSource.updateBoardLayout(roomCode, localPlayer.getId(), placements, new DataCallback<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String error) {
                    System.out.println("[GameController] Failed to upload board layout: " + error);
                }
            });

            // Mark player as ready
            gameDataSource.updatePlacementStatus(roomCode, localPlayer.getId(), true, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Local status update if needed
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("[GameController] Failed to confirm placement: " + error);
                }
            });
        }
    }

    public void addPlacementStatusListener(DataCallback<Boolean> callback) {
        if (isSinglePlayer) {
            // In single player, the bot is ready as soon as it's initialized
            callback.onSuccess(true);
            // Also notify the main game listener so the UI updates immediately
            if (listener != null) {
                listener.onOpponentPlacementReady(true);
            }
        } else if (roomCode != null && remotePlayer != null) {
            gameDataSource.addPlacementStatusListener(roomCode, remotePlayer.getId(), new DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean ready) {
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if (listener != null) {
                            listener.onOpponentPlacementReady(ready);
                        }
                        callback.onSuccess(ready);
                    });
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        }
    }

    public void removeShipAt(Coordinate coordinate) {
        requireSession();
        battleships_ex.gdx.model.board.Cell cell = localPlayer.getBoard().getCell(coordinate);
        if (cell.hasShip()) {
            Ship ship = cell.getShip();
            // Notify listener before removal while coordinates are still intact
            if (listener != null) {
                listener.onShipRemoved(ship);
            }
            localPlayer.getBoard().removeShip(ship);
        }
    }

    public void playActionCard(battleships_ex.gdx.model.cards.ActionCard card) {
        playActionCard(card, null);
    }

    public void playActionCard(battleships_ex.gdx.model.cards.ActionCard card, Coordinate target) {
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) return;

        if (!card.canUse(localPlayer, remotePlayer)) {
            String reason = "INSUFFICIENT ENERGY";
            if (listener != null) listener.onActionCardRejected(card, reason);
            return;
        }

        // If card requires targeting but no target provided, ask UI
        if (target == null && (card instanceof battleships_ex.gdx.model.cards.SonarCard ||
                               card instanceof battleships_ex.gdx.model.cards.BombCard ||
                               card instanceof battleships_ex.gdx.model.cards.MineCard ||
                               card instanceof battleships_ex.gdx.model.cards.AirstrikeCard)) {
            if (listener != null) listener.onCardTargetRequested(card);
            return;
        }

        battleships_ex.gdx.model.cards.ActionCardResult result = session.playActionCard(card, target);

        syncActionCardToBackend(card, target);

        if (result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.HIT ||
            result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.SUNK) {
            remotePlayer.addEnergy(1);
        }

        if (Boolean.TRUE.equals(result.getMetadata("mineHit"))) {
            // Local player triggered a remote mine via action card
            handleMineHit(localPlayer);
        }

        if (listener != null) listener.onActionCardPlayed(result);

        if (card.endsTurn()) {
            if (roomCode != null) {
                syncTurnToBackend();
            }
            if (listener != null) {
                listener.onTurnChanged(session.getCurrentPlayer().getId());
            }
            if (isSinglePlayer && session.getCurrentPlayer() == remotePlayer) {
                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override public void run() { playBotTurn(); }
                }, 1.0f);
            }
        }

        // Reset inactivity timer on action
        sessionManager.resetInactivityTimer();

        // Sync with backend if needed
        // For now, moves from cards are not automatically synced as moves,
        // but the board state changes should be reflected.
        // TODO: Implement card effect synchronization

        // Check win condition - some cards (e.g. Airstrike) can sink ships
        if (result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.SUNK
            && engine.hasWon(remotePlayer.getBoard())) {
            notify_gameOver(localPlayer.getName());
            if (roomCode != null) {
                gameDataSource.pushGameOver(roomCode, localPlayer.getName(), new DataCallback<Void>() {
                    @Override public void onSuccess(Void r) {}
                    @Override public void onFailure(String error) {
                        System.out.println("[GameController] Failed to push game over: " + error);
                    }
                });
            }
        }
    }

    public void fireShot(int row, int col) {
        System.out.println("[GameController] LOG: fireShot called at (" + row + ", " + col + ")");
        if (!isSessionActive())   return;
        if (!isLocalPlayerTurn()) {
            System.out.println("[GameController] LOG: Blocked fireShot because it's not local player's turn.");
            return;
        }

        Coordinate target = new Coordinate(row, col);

        // ---- SHIELD INTERCEPTION (REMOTE PLAYER) ----
        if (remotePlayer.hasShield()) {
            remotePlayer.consumeShield();

            // Treat as MISS
            clearPreview();
            sessionManager.resetInactivityTimer();

            session.processMove(target, ShotResult.miss(target));
            notify_miss(target);
            syncMoveToBackend(target, false);
            syncTurnToBackend();
            return;
        }

        ShotResult result  = engine.resolveShot(remotePlayer.getBoard(), target);

        // Clear preview now that shot is confirmed (Issue #28)
        clearPreview();

        // Reset inactivity timer on action
        sessionManager.resetInactivityTimer();

        switch (result.getOutcome()) {

            case ALREADY_SHOT:
                System.out.println("[GameController] LOG: Shot outcome: ALREADY_SHOT");
                notify_alreadyShot(target);
                return;

            case MISS:
                System.out.println("[GameController] LOG: Shot outcome: MISS");
                session.processMove(target, result);    // records move + switches turn
                notify_miss(target);
                syncMoveToBackend(target, false);
                syncTurnToBackend();
                break;

            case HIT:
                System.out.println("[GameController] LOG: Shot outcome: HIT");
                remotePlayer.addEnergy(1);
                session.processMove(target, result);    // records move; turn stays (hit-again)
                notify_hit(target, result.getSunkShip());
                syncMoveToBackend(target, true);
                break;

            case SUNK:
                System.out.println("[GameController] LOG: Shot outcome: SUNK");
                remotePlayer.addEnergy(1);
                session.processMove(target, result);
                notify_sunk(target, result.getSunkShip());
                syncMoveToBackend(target, true);

                if (engine.hasWon(remotePlayer.getBoard())) {
                    notify_gameOver(localPlayer.getName());
                    if (roomCode != null) {
                        gameDataSource.pushGameOver(roomCode, localPlayer.getName(), new DataCallback<Void>() {
                            @Override public void onSuccess(Void r) {}
                            @Override public void onFailure(String error) {
                                System.out.println("[GameController] Failed to push game over: " + error);
                            }
                        });
                    }
                }
                break;

            case MINE_HIT:
                System.out.println("[GameController] LOG: Shot outcome: MINE_HIT");
                session.processMove(target, result);
                // localPlayer hit a remote mine. localPlayer gets hit by 2 random shots.
                handleMineHit(localPlayer);
                syncMoveToBackend(target, true);
                break;
        }
    }

    public void onRemoteShotReceived(int row, int col) {
        System.out.println("[GameController] LOG: onRemoteShotReceived at (" + row + ", " + col + ")");
        if (!isSessionActive()) return;

        Coordinate target = new Coordinate(row, col);

        // ---- SHIELD INTERCEPTION (LOCAL PLAYER) ----
        if (localPlayer.hasShield()) {
            localPlayer.consumeShield();
            // Manually create a MISS result to process
            session.processMove(target, ShotResult.miss(target));
            return;
        }

        ShotResult result  = engine.resolveShot(localPlayer.getBoard(), target);

        switch (result.getOutcome()) {

            case MISS:
                session.processMove(target, result);    // switches turn back to local
                notify_miss(target);
                break;

            case HIT:
                localPlayer.addEnergy(1);
                session.processMove(target, result);
                notify_hit(target, result.getSunkShip());
                break;
            case SUNK:
                localPlayer.addEnergy(1);
                session.processMove(target, result);
                notify_sunk(target, result.getSunkShip());
                if (result.isSunk() && engine.hasWon(localPlayer.getBoard())) {
                    notify_gameOver(remotePlayer.getName());
                }
                break;

            case MINE_HIT:
                session.processMove(target, result);
                // Remote player hit a local mine. They get hit by 2 random shots.
                handleMineHit(remotePlayer);
                break;

            case ALREADY_SHOT:
                // Stale Firebase event — ignore silently.
                break;
        }
    }

    private void handleMineHit(Player attacker) {
        // Trigger 2 random shots at the attacker's board
        List<ShotResult> counterShots = engine.triggerRandomShots(attacker, 2);
        for (ShotResult sr : counterShots) {
            if (sr.isShipHit()) {
                attacker.addEnergy(1);
            }
            if (listener != null) {
                battleships_ex.gdx.model.cards.ActionCardResult detox =
                    battleships_ex.gdx.model.cards.ActionCardResult.hit("Mine Detonation",
                    java.util.Collections.singletonList(sr.getCoordinate()));
                listener.onActionCardPlayed(detox);
            }
            // If we are local, and we just hit a remote mine, we need to sync our own hits.
            // If the remote player hit our mine, we (local) are firing shots at them (attacker).
            // Actually, if we are in onRemoteShotReceived, the remote player (attacker) hit OUR mine.
            // So we (local) should fire back at them.
            if (attacker == remotePlayer) {
                 syncMoveToBackend(sr.getCoordinate(), sr.isShipHit());
            }
        }
    }

    /**
     * Cleans up all real-time listeners and session state.
     * Call when leaving the battle screen.
     */
    public void cleanup() {
        sessionManager.endSession();
        isSinglePlayer = false; // Issue #4/5: ensure single-player flag is reset when cleaning up
        if (roomCode != null) {
            gameDataSource.removeAllListeners(roomCode);
        }
    }

    public boolean isLocalPlayerTurn() {
        return session != null
            && session.isStarted()
            && session.getCurrentPlayer() == localPlayer;
    }

    /** @return true if a session is active and not yet over */
    public boolean isSessionActive() {
        return session != null && session.isStarted();
    }

    /** @return the current GameSession, or null if not yet initialised */
    public GameSession getSession() {
        return session;
    }

    /** @return the current room code */
    public String getRoomCode() {
        return roomCode;
    }

    // ── Backend sync helpers ────────────────────────────────────────

    private void syncMoveToBackend(Coordinate target, boolean hit) {
        if (roomCode == null) return;

        gameDataSource.submitMove(roomCode, localPlayer.getId(), target, hit, new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String error) {
                System.out.println("[GameController] Failed to sync move: " + error);
            }
        });
    }

    private void syncTurnToBackend() {
        if (roomCode == null) return;
        System.out.println("[GameController] LOG: Syncing turn to backend. New turn: " + session.getCurrentPlayer().getId());

        gameDataSource.syncTurn(roomCode, session.getCurrentPlayer().getId(), new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String error) {
                System.out.println("[GameController] Failed to sync turn: " + error);
            }
        });
    }

    // ── Notification helpers ────────────────────────────────────────

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

    private void onActionCardReceived(GameDataSource.ActionCardSnapshot snapshot) {
        System.out.println("[GameController] LOG: Action card received: " + snapshot.cardName + " from " + snapshot.playerId);

        // Find card in remote player's hand by name
        battleships_ex.gdx.model.cards.ActionCard cardToPlay = null;
        for (battleships_ex.gdx.model.cards.ActionCard c : remotePlayer.getCards()) {
            if (c.getClass().getSimpleName().startsWith(snapshot.cardName)) {
                cardToPlay = c;
                break;
            }
        }

        if (cardToPlay == null) {
            System.err.println("[GameController] Opponent played card they don't have: " + snapshot.cardName);
            // Fallback: create a new instance if needed for sync (though they should have it)
            cardToPlay = battleships_ex.gdx.model.cards.ActionCardRegistry.createCard(snapshot.cardName);
            remotePlayer.addCard(cardToPlay);
        }

        // Apply metadata (e.g. orientation)
        if (cardToPlay instanceof battleships_ex.gdx.model.cards.AirstrikeCard && snapshot.metadata != null) {
            battleships_ex.gdx.model.cards.AirstrikeCard ac = (battleships_ex.gdx.model.cards.AirstrikeCard) cardToPlay;
            if (!ac.getOrientation().name().equals(snapshot.metadata)) {
                ac.toggleOrientation();
            }
        }

        // Execute card from remote player perspective
        battleships_ex.gdx.model.cards.ActionCardResult result = session.executeActionCardPlay(cardToPlay, snapshot.target);

        // Grant energy to local player if they were hit
        if (result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.HIT ||
            result.getOutcome() == battleships_ex.gdx.model.cards.ActionCardResult.Outcome.SUNK) {
            localPlayer.addEnergy(1);
        }

        if (Boolean.TRUE.equals(result.getMetadata("mineHit"))) {
            // Remote player triggered a local mine via action card
            handleMineHit(remotePlayer);
        }

        if (listener != null) listener.onActionCardPlayed(result);

        if (cardToPlay.endsTurn()) {
            if (roomCode != null) {
                syncTurnToBackend();
            }
            if (listener != null) {
                listener.onTurnChanged(session.getCurrentPlayer().getId());
            }
            if (isSinglePlayer && session.getCurrentPlayer() == remotePlayer) {
                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override public void run() { playBotTurn(); }
                }, 1.0f);
            }
        }
    }

    private void syncActionCardToBackend(battleships_ex.gdx.model.cards.ActionCard card, Coordinate target) {
        if (roomCode == null) return;

        String cardName = card.getClass().getSimpleName().replace("Card", "");
        String metadata = null;
        if (card instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
            metadata = ((battleships_ex.gdx.model.cards.AirstrikeCard) card).getOrientation().name();
        }

        gameDataSource.submitActionCardPlay(roomCode, localPlayer.getId(), cardName, target, metadata, new DataCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String error) {
                System.err.println("[GameController] Failed to sync action card: " + error);
            }
        });
    }

    private void requireSession() {
        if (session == null) {
            throw new IllegalStateException(
                "GameController: initSession() must be called before this operation.");
        }
    }
    /**
     * Initialises a local-only session against a simple computer opponent.
     * Reuses the stubbed Data Source natively via initSession.
     */
    public void initSinglePlayerSession(Player local) {
        this.isSinglePlayer = true;

        Player remote = new Player("BOT", "Computer");
        this.botLogic = new SimpleBot();
        this.botLogic.placeHardcodedShips(remote.getBoard());

        debugPrintEnemyBoard(remote.getBoard());

        // Using the overloaded initSession without a room code ensures
        // it functions locally (usually leveraging StubGameDataSource).
        initSession(local, remote);
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    /**
     * Fetches the next predetermined move and feeds it into the standard remote shot pipeline.
     */
    public void playBotTurn() {
        if (botLogic == null || !isSessionActive() || session.gameIsOver()) return;

        Coordinate target = botLogic.getNextMove();
        onRemoteShotReceived(target.getRow(), target.getCol());
    }
    private void debugPrintEnemyBoard(battleships_ex.gdx.model.board.Board board) {
        System.out.println("=== ENEMY BOT BOARD ===");
        for (int row = 0; row < board.getHeight(); row++) {
            StringBuilder rowString = new StringBuilder();
            for (int col = 0; col < board.getWidth(); col++) {
                // Check if the cell has a ship using the methods from Board.java
                if (board.getCell(new Coordinate(row, col)).hasShip()) {
                    rowString.append("[S]"); // S for Ship
                } else {
                    rowString.append("[ ]"); // Empty
                }
            }
            System.out.println(rowString);
        }
        System.out.println("=======================");
    }
}
