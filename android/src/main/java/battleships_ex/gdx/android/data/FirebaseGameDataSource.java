package battleships_ex.gdx.android.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.model.board.Coordinate;

/**
 * Firebase Realtime Database implementation of {@link GameDataSource}.
 * Handles real-time game state synchronization: moves, turns, heartbeat.
 *
 * DB structure:
 * <pre>
 * rooms/{roomCode}/
 *   game/
 *     currentTurn:   String (playerId)
 *     status:        "placing" | "playing" | "finished"
 *     winner:        String (winnerName) | null
 *     moves/
 *       {pushId}:    { playerId, row, col, hit, timestamp }
 *     heartbeat/
 *       {playerId}:  { lastSeen: long }
 * </pre>
 */
public class FirebaseGameDataSource implements GameDataSource {

    private static final String NODE_GAME        = "game";
    private static final String NODE_MOVES       = "moves";
    private static final String NODE_HEARTBEAT   = "heartbeat";
    private static final String FIELD_CURRENT_TURN = "currentTurn";
    private static final String FIELD_STATUS     = "status";
    private static final String FIELD_WINNER     = "winner";
    private static final String FIELD_PLAYER_ID  = "playerId";
    private static final String FIELD_ROW        = "row";
    private static final String FIELD_COL        = "col";
    private static final String FIELD_HIT        = "hit";
    private static final String FIELD_TIMESTAMP  = "timestamp";
    private static final String FIELD_LAST_SEEN  = "lastSeen";

    /** Opponent is considered disconnected after 15 seconds without heartbeat. */
    private static final long HEARTBEAT_STALE_MS = 15_000L;

    private final DatabaseReference roomsRef;
    private static final String NODE_PREVIEW    = "preview";

    private final Map<String, ValueEventListener> moveListeners      = new HashMap<>();
    private final Map<String, ValueEventListener> turnListeners      = new HashMap<>();
    private final Map<String, ValueEventListener> heartbeatListeners = new HashMap<>();
    private final Map<String, ValueEventListener> previewListeners   = new HashMap<>();

    public FirebaseGameDataSource() {
        this.roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
    }

    // ── Game reference helper ───────────────────────────────────────

    private DatabaseReference gameRef(String roomCode) {
        return roomsRef.child(roomCode).child(NODE_GAME);
    }

    // ── Move synchronization ────────────────────────────────────────

    @Override
    public void submitMove(String roomCode, String playerId, Coordinate target,
                           boolean hit, DataCallback<Void> callback) {
        DatabaseReference movesRef = gameRef(roomCode).child(NODE_MOVES).push();

        Map<String, Object> moveData = new HashMap<>();
        moveData.put(FIELD_PLAYER_ID, playerId);
        moveData.put(FIELD_ROW,       target.getRow());
        moveData.put(FIELD_COL,       target.getCol());
        moveData.put(FIELD_HIT,       hit);
        moveData.put(FIELD_TIMESTAMP, ServerValue.TIMESTAMP);

        movesRef.setValue(moveData)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback) {
        removeMoveListener(roomCode);

        DatabaseReference movesRef = gameRef(roomCode).child(NODE_MOVES);

        // Only listen for new children added after registration
        ValueEventListener listener = new ValueEventListener() {
            private boolean initialLoadDone = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!initialLoadDone) {
                    // Skip the initial load of existing moves
                    initialLoadDone = true;
                    return;
                }

                // Process latest move
                DataSnapshot lastChild = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    lastChild = child;
                }
                if (lastChild == null) return;

                String pid = lastChild.child(FIELD_PLAYER_ID).getValue(String.class);
                Long row   = lastChild.child(FIELD_ROW).getValue(Long.class);
                Long col   = lastChild.child(FIELD_COL).getValue(Long.class);
                Boolean h  = lastChild.child(FIELD_HIT).getValue(Boolean.class);
                Long ts    = lastChild.child(FIELD_TIMESTAMP).getValue(Long.class);

                if (pid == null || row == null || col == null || h == null) return;

                callback.onSuccess(new MoveSnapshot(
                    pid, row.intValue(), col.intValue(), h,
                    ts != null ? ts : System.currentTimeMillis()
                ));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        moveListeners.put(roomCode, listener);
        movesRef.addValueEventListener(listener);
    }

    @Override
    public void removeMoveListener(String roomCode) {
        ValueEventListener listener = moveListeners.remove(roomCode);
        if (listener != null) {
            gameRef(roomCode).child(NODE_MOVES).removeEventListener(listener);
        }
    }

    // ── Turn synchronization ────────────────────────────────────────

    @Override
    public void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback) {
        gameRef(roomCode).child(FIELD_CURRENT_TURN).setValue(currentPlayerId)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void addTurnListener(String roomCode, DataCallback<String> callback) {
        removeTurnListener(roomCode);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String turnPlayerId = snapshot.getValue(String.class);
                if (turnPlayerId != null) {
                    callback.onSuccess(turnPlayerId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        turnListeners.put(roomCode, listener);
        gameRef(roomCode).child(FIELD_CURRENT_TURN).addValueEventListener(listener);
    }

    @Override
    public void removeTurnListener(String roomCode) {
        ValueEventListener listener = turnListeners.remove(roomCode);
        if (listener != null) {
            gameRef(roomCode).child(FIELD_CURRENT_TURN).removeEventListener(listener);
        }
    }

    // ── Game status ─────────────────────────────────────────────────

    @Override
    public void updateGameStatus(String roomCode, String status, DataCallback<Void> callback) {
        gameRef(roomCode).child(FIELD_STATUS).setValue(status)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_STATUS, "finished");
        updates.put(FIELD_WINNER, winnerName);

        gameRef(roomCode).updateChildren(updates)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Heartbeat / session health ──────────────────────────────────

    @Override
    public void sendHeartbeat(String roomCode, String playerId) {
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put(FIELD_LAST_SEEN, ServerValue.TIMESTAMP);

        gameRef(roomCode).child(NODE_HEARTBEAT).child(playerId)
            .updateChildren(heartbeat);

        // Set onDisconnect to mark the player as stale immediately
        gameRef(roomCode).child(NODE_HEARTBEAT).child(playerId)
            .child(FIELD_LAST_SEEN)
            .onDisconnect()
            .setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void addHeartbeatListener(String roomCode, String opponentId,
                                     DataCallback<Boolean> callback) {
        removeHeartbeatListener(roomCode);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastSeen = snapshot.child(FIELD_LAST_SEEN).getValue(Long.class);

                if (lastSeen == null) {
                    // No heartbeat yet — opponent hasn't connected
                    callback.onSuccess(true);
                    return;
                }

                long elapsed = System.currentTimeMillis() - lastSeen;
                callback.onSuccess(elapsed > HEARTBEAT_STALE_MS);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        heartbeatListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_HEARTBEAT).child(opponentId)
            .addValueEventListener(listener);
    }

    @Override
    public void removeHeartbeatListener(String roomCode) {
        ValueEventListener listener = heartbeatListeners.remove(roomCode);
        if (listener != null) {
            // Remove from all potential heartbeat paths
            gameRef(roomCode).child(NODE_HEARTBEAT).removeEventListener(listener);
        }
    }

    // ── Target preview (Issue #28) ────────────────────────────────

    @Override
    public void sendPreview(String roomCode, String playerId, Coordinate target) {
        Map<String, Object> previewData = new HashMap<>();
        previewData.put(FIELD_ROW, target.getRow());
        previewData.put(FIELD_COL, target.getCol());
        previewData.put(FIELD_TIMESTAMP, ServerValue.TIMESTAMP);

        gameRef(roomCode).child(NODE_PREVIEW).child(playerId)
            .updateChildren(previewData);
    }

    @Override
    public void clearPreview(String roomCode, String playerId) {
        gameRef(roomCode).child(NODE_PREVIEW).child(playerId).removeValue();
    }

    @Override
    public void addPreviewListener(String roomCode, String opponentId,
                                   DataCallback<Coordinate> callback) {
        removePreviewListener(roomCode);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(null); // preview cleared
                    return;
                }

                Long row = snapshot.child(FIELD_ROW).getValue(Long.class);
                Long col = snapshot.child(FIELD_COL).getValue(Long.class);

                if (row != null && col != null) {
                    callback.onSuccess(new Coordinate(row.intValue(), col.intValue()));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        previewListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_PREVIEW).child(opponentId)
            .addValueEventListener(listener);
    }

    @Override
    public void removePreviewListener(String roomCode) {
        ValueEventListener listener = previewListeners.remove(roomCode);
        if (listener != null) {
            gameRef(roomCode).child(NODE_PREVIEW).removeEventListener(listener);
        }
    }

    // ── Session re-join & cleanup (Issue #29) ─────────────────────

    @Override
    public void roomIsActive(String roomCode, DataCallback<Boolean> callback) {
        gameRef(roomCode).child(FIELD_STATUS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(false);
                    return;
                }
                String status = snapshot.getValue(String.class);
                boolean active = "placing".equals(status) || "playing".equals(status);
                callback.onSuccess(active);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void loadGameState(String roomCode, DataCallback<GameSnapshot> callback) {
        gameRef(roomCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Game state not found");
                    return;
                }

                String currentTurn = snapshot.child(FIELD_CURRENT_TURN).getValue(String.class);
                String status      = snapshot.child(FIELD_STATUS).getValue(String.class);

                callback.onSuccess(new GameSnapshot(currentTurn, status != null ? status : "finished"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void cleanupSession(String roomCode, DataCallback<Void> callback) {
        gameRef(roomCode).child(FIELD_STATUS).setValue("abandoned")
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Cleanup ─────────────────────────────────────────────────────

    @Override
    public void removeAllListeners(String roomCode) {
        removeMoveListener(roomCode);
        removeTurnListener(roomCode);
        removeHeartbeatListener(roomCode);
        removePreviewListener(roomCode);
    }
}
