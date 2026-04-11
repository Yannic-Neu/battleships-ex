package battleships_ex.gdx.controller;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;
import battleships_ex.gdx.data.LobbyDataSource.LobbySnapshot;
import battleships_ex.gdx.data.RoomCodeGenerator;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.lobby.Lobby;

public class LobbyController {

    private static final int    CODE_LENGTH  = 6;
    private static final String CODE_CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public enum JoinRejectionReason {
        INVALID_CODE_FORMAT,
        LOBBY_NOT_FOUND,
        LOBBY_FULL,
        GAME_ALREADY_STARTED,
        BACKEND_ERROR
    }

    private final LobbyDataSource dataSource;

    private Lobby         activeLobby;
    private Player        localPlayer;
    private LobbyListener listener;

    public LobbyController(LobbyDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setListener(LobbyListener listener) {
        this.listener = listener;
    }

    /**
     * HOST
     * Creates a new lobby for the local player, who becomes the host.
     *
     * Steps:
     *   1. Generate a room code via {@link RoomCodeGenerator}.
     *   2. Write the lobby to the backend via {@link LobbyDataSource#createLobby}.
     *   3. On success: build the local Lobby domain object, start the real-time
     *      listener, and notify the View.
     *   4. On failure: surface BACKEND_ERROR to the View.
     *
     * The real-time listener is attached immediately so the host detects when
     * the guest joins without polling.
     *
     * @param local the local player who will host
     */
    public void createLobby(Player local) {
        this.localPlayer = local;

        String roomCode = RoomCodeGenerator.generate();

        dataSource.createLobby(roomCode, local.getId(), local.getName(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                long  lobbyId = System.currentTimeMillis();
                Lobby lobby   = new Lobby(lobbyId, roomCode);
                lobby.addPlayer(local);
                activeLobby = lobby;

                attachLobbyListener(roomCode);
                notify_lobbyCreated(lobby, roomCode);
            }

            @Override
            public void onFailure(String errorMessage) {
                notify_joinRejected(JoinRejectionReason.BACKEND_ERROR);
            }
        });
    }

    /**
     * GUEST
     * Attempts to join an existing lobby using the provided room code.
     *
     * Validation order (short-circuit on first failure):
     *   1. Format check (local, no network) — length and charset.
     *   2. Backend existence check via {@link LobbyDataSource#lobbyExists}.
     *   3. Snapshot fetch to check capacity, started flag, and re-entry.
     *   4. Re-entry: player id matches host or guest → restore slot.
     *   5. Started guard → GAME_ALREADY_STARTED.
     *   6. Capacity guard → LOBBY_FULL.
     *   7. All clear → call {@link LobbyDataSource#joinLobby} and attach listener.
     *
     * @param local   the local player attempting to join
     * @param rawCode the room code exactly as the player typed it (trimmed internally)
     */
    public void joinLobby(Player local, String rawCode) {
        this.localPlayer = local;

        // ---- 1. Format validation (local, no network) -------------------
        String code = normalise(rawCode);
        if (!isValidFormat(code)) {
            notify_joinRejected(JoinRejectionReason.INVALID_CODE_FORMAT);
            return;
        }

        // ---- 2. Backend existence check ---------------------------------
        dataSource.lobbyExists(code, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!exists) {
                    notify_joinRejected(JoinRejectionReason.LOBBY_NOT_FOUND);
                    return;
                }
                // ---- 3. Fetch snapshot for detailed checks --------------
                fetchAndJoin(local, code);
            }

            @Override
            public void onFailure(String errorMessage) {
                notify_joinRejected(JoinRejectionReason.BACKEND_ERROR);
            }
        });
    }

    /**
     * Called when the host confirms they are ready to start.
     * Updates the lobby status to "ready" on the backend so all participants
     * transition to the deployment phase.
     */
    public void confirmReady() {
        if (activeLobby == null || !activeLobby.isReady()) return;
        if (!isLocalPlayerHost())                           return;

        dataSource.setLobbyStatus(activeLobby.getRoomCode(), "ready", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // The local listener will catch the status change and call notify_lobbyReady()
            }

            @Override
            public void onFailure(String error) {
                System.err.println("[LobbyController] Failed to set lobby ready: " + error);
            }
        });
    }

    public void onSnapshotUpdated(LobbySnapshot snapshot) {
        if (activeLobby == null || snapshot == null) return;

        // Ready / playing transition
        if ("ready".equals(snapshot.status) || "playing".equals(snapshot.status)) {
            notify_lobbyReady();
            return;
        }

        // Guest joined — only relevant on the host's device
        if (snapshot.isFull() && activeLobby.getGuest() == null && isLocalPlayerHost()) {
            Player guest = new Player(snapshot.guestPlayerId, snapshot.guestPlayerName);
            try {
                activeLobby.addPlayer(guest);
            } catch (IllegalStateException ignored) {
                // Snapshot fired twice — guest already registered, safe to ignore
            }
            notify_guestJoined(guest);
            return;
        }

        // Guest disconnected — guest slot cleared in snapshot
        if (!snapshot.isFull() && activeLobby.getGuest() != null) {
            rebuildLobbyHostOnly();
            notify_opponentDisconnected();
        }
    }

    public void onRemoteLobbyReady() {
        notify_lobbyReady();
    }

    public void onRemotePlayerDisconnected() {
        rebuildLobbyHostOnly();
        notify_opponentDisconnected();
    }

    /**
     * Detaches the real-time listener and clears active lobby state.
     * Call when the player leaves the lobby screen without starting a game.
     */
    public void leaveLobby() {
        if (activeLobby == null || localPlayer == null) return;

        String roomCode = activeLobby.getRoomCode();
        dataSource.removeLobbyListener(roomCode);
        dataSource.leaveLobby(roomCode, localPlayer.getId(), new DataCallback<Void>() {
            @Override public void onSuccess(Void r)       { /* no-op */ }
            @Override public void onFailure(String error) { /* no-op */ }
        });

        activeLobby = null;
        localPlayer = null;
    }

    /** @return the active Lobby, or null if none has been created or joined */
    public Lobby getActiveLobby() {
        return activeLobby;
    }

    /** @return true if the local player is the host of the active lobby */
    public boolean isLocalPlayerHost() {
        return activeLobby != null
            && localPlayer != null
            && activeLobby.getHost() != null
            && activeLobby.getHost().getId().equals(localPlayer.getId());
    }

    private void fetchAndJoin(Player local, String code) {
        dataSource.addLobbyListener(code, new DataCallback<LobbySnapshot>() {
            @Override
            public void onSuccess(LobbySnapshot snapshot) {
                // Remove the one-shot listener; persistent listener is attached
                // later only if the join succeeds.
                dataSource.removeLobbyListener(code);
                evaluateSnapshot(local, code, snapshot);
            }

            @Override
            public void onFailure(String errorMessage) {
                notify_joinRejected(JoinRejectionReason.BACKEND_ERROR);
            }
        });
    }

    /**
     * Applies checks 4–7 against the fetched snapshot and either rejects
     * the join attempt or completes it.
     */
    private void evaluateSnapshot(Player local, String code, LobbySnapshot snapshot) {
        String hostId  = snapshot.hostPlayerId;
        String guestId = snapshot.guestPlayerId;   // null if guest slot is empty

        // ---- 4. Re-entry check ------------------------------------------
        // String equality — Player#getId() returns String
        boolean returningHost  = local.getId().equals(hostId);
        boolean returningGuest = guestId != null && local.getId().equals(guestId);

        if (returningHost || returningGuest) {
            restoreFromSnapshot(local, code, snapshot, returningHost);
            return;
        }

        // ---- 5. Game-started guard --------------------------------------
        if ("playing".equals(snapshot.status)) {
            notify_joinRejected(JoinRejectionReason.GAME_ALREADY_STARTED);
            return;
        }

        // ---- 6. Capacity check ------------------------------------------
        if (snapshot.isFull()) {
            notify_joinRejected(JoinRejectionReason.LOBBY_FULL);
            return;
        }

        // ---- 7. All clear — write to backend and build local state ------
        dataSource.joinLobby(code, local.getId(), local.getName(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Snapshot carries both id and name — construct Player properly
                Player host  = new Player(hostId, snapshot.hostPlayerName);
                long   id    = System.currentTimeMillis();
                Lobby  lobby = new Lobby(id, code);
                lobby.addPlayer(host);
                lobby.addPlayer(local);
                activeLobby = lobby;

                attachLobbyListener(code);
                notify_lobbyJoined(lobby);
            }

            @Override
            public void onFailure(String errorMessage) {
                notify_joinRejected(JoinRejectionReason.BACKEND_ERROR);
            }
        });
    }

    private void restoreFromSnapshot(Player local, String code,
                                     LobbySnapshot snapshot, boolean isHost) {
        long  id    = System.currentTimeMillis();
        Lobby lobby = new Lobby(id, code);

        if (isHost) {
            lobby.addPlayer(local);
            if (snapshot.guestPlayerId != null) {
                // Snapshot carries name — construct guest Player properly
                lobby.addPlayer(new Player(snapshot.guestPlayerId, snapshot.guestPlayerName));
            }
        } else {
            // Snapshot carries name — construct host Player properly
            lobby.addPlayer(new Player(snapshot.hostPlayerId, snapshot.hostPlayerName));
            lobby.addPlayer(local);
        }

        activeLobby = lobby;
        attachLobbyListener(code);
        notify_lobbyJoined(lobby);

        // If both slots filled after restore, fire guestJoined so host View
        // refreshes its "waiting" state.
        if (lobby.isReady() && !isHost) {
            notify_guestJoined(local);
        }
    }

    private void attachLobbyListener(String roomCode) {
        dataSource.addLobbyListener(roomCode, new DataCallback<LobbySnapshot>() {
            @Override
            public void onSuccess(LobbySnapshot snapshot) {
                onSnapshotUpdated(snapshot);
            }

            @Override
            public void onFailure(String errorMessage) {
                notify_opponentDisconnected();
            }
        });
    }

    private void rebuildLobbyHostOnly() {
        if (activeLobby == null) return;
        Lobby refreshed = new Lobby(activeLobby.getId(), activeLobby.getRoomCode());
        refreshed.addPlayer(activeLobby.getHost());
        activeLobby = refreshed;
    }

    private boolean isValidFormat(String code) {
        if (code.length() != CODE_LENGTH) return false;
        for (char c : code.toCharArray()) {
            if (CODE_CHARSET.indexOf(c) < 0) return false;
        }
        return true;
    }

    private String normalise(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    private void notify_lobbyCreated(Lobby lobby, String roomCode) {
        if (listener != null) listener.onLobbyCreated(lobby, roomCode);
    }

    private void notify_lobbyJoined(Lobby lobby) {
        if (listener != null) listener.onLobbyJoined(lobby);
    }

    private void notify_guestJoined(Player guest) {
        if (listener != null) listener.onGuestJoined(guest);
    }

    private void notify_lobbyReady() {
        if (listener != null) listener.onLobbyReady();
    }

    private void notify_joinRejected(JoinRejectionReason reason) {
        if (listener != null) listener.onJoinRejected(reason);
    }

    private void notify_opponentDisconnected() {
        if (listener != null) listener.onOpponentDisconnected();
    }
}
