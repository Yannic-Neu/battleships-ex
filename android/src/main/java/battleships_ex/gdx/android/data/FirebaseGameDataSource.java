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
 */
public class FirebaseGameDataSource implements GameDataSource {

    private static final String NODE_GAME        = "game";
    private static final String NODE_MOVES       = "moves";
    private static final String NODE_CARDS       = "cards";
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

    private static final long HEARTBEAT_STALE_MS = 15_000L;

    private final DatabaseReference roomsRef;
    private static final String NODE_PREVIEW    = "preview";
    private static final String NODE_PLACEMENT_READY = "placementReady";
    private static final String NODE_BOARDS = "boards";

    private final Map<String, ValueEventListener> moveListeners      = new HashMap<>();
    private final Map<String, ValueEventListener> turnListeners      = new HashMap<>();
    private final Map<String, ValueEventListener> heartbeatListeners = new HashMap<>();
    private final Map<String, ValueEventListener> previewListeners   = new HashMap<>();
    private final Map<String, ValueEventListener> placementStatusListeners = new HashMap<>();
    private final Map<String, ValueEventListener> boardLayoutListeners = new HashMap<>();
    private final Map<String, ValueEventListener> actionCardListeners = new HashMap<>();
    private final Map<String, ValueEventListener> statusListeners = new HashMap<>();

    public FirebaseGameDataSource() {
        this.roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
    }

    private DatabaseReference gameRef(String roomCode) {
        return roomsRef.child(roomCode).child(NODE_GAME);
    }

    @Override
    public void addStatusListener(String roomCode, DataCallback<String> callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) callback.onSuccess(status);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        statusListeners.put(roomCode, listener);
        gameRef(roomCode).child(FIELD_STATUS).addValueEventListener(listener);
    }

    private void removeStatusListener(String roomCode) {
        ValueEventListener listener = statusListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(FIELD_STATUS).removeEventListener(listener);
    }

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
        ValueEventListener listener = new ValueEventListener() {
            private boolean initialLoadDone = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!initialLoadDone) { initialLoadDone = true; return; }
                DataSnapshot lastChild = null;
                for (DataSnapshot child : snapshot.getChildren()) lastChild = child;
                if (lastChild == null) return;
                String pid = lastChild.child(FIELD_PLAYER_ID).getValue(String.class);
                Long row   = lastChild.child(FIELD_ROW).getValue(Long.class);
                Long col   = lastChild.child(FIELD_COL).getValue(Long.class);
                Boolean h  = lastChild.child(FIELD_HIT).getValue(Boolean.class);
                Long ts    = lastChild.child(FIELD_TIMESTAMP).getValue(Long.class);
                if (pid == null || row == null || col == null || h == null) return;
                callback.onSuccess(new MoveSnapshot(pid, row.intValue(), col.intValue(), h, ts != null ? ts : System.currentTimeMillis()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        moveListeners.put(roomCode, listener);
        movesRef.addValueEventListener(listener);
    }

    @Override
    public void removeMoveListener(String roomCode) {
        ValueEventListener listener = moveListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_MOVES).removeEventListener(listener);
    }

    @Override
    public void submitActionCardPlay(String roomCode, String playerId, String cardName, Coordinate target, String metadata, DataCallback<Void> callback) {
        DatabaseReference cardsRef = gameRef(roomCode).child(NODE_CARDS).push();
        Map<String, Object> cardData = new HashMap<>();
        cardData.put(FIELD_PLAYER_ID, playerId);
        cardData.put("cardName",      cardName);
        if (target != null) {
            cardData.put(FIELD_ROW,   target.getRow());
            cardData.put(FIELD_COL,   target.getCol());
        }
        if (metadata != null) cardData.put("metadata",  metadata);
        cardData.put(FIELD_TIMESTAMP, ServerValue.TIMESTAMP);
        cardsRef.setValue(cardData)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void addActionCardListener(String roomCode, DataCallback<ActionCardSnapshot> callback) {
        removeActionCardListener(roomCode);
        DatabaseReference cardsRef = gameRef(roomCode).child(NODE_CARDS);
        ValueEventListener listener = new ValueEventListener() {
            private boolean initialLoadDone = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!initialLoadDone) { initialLoadDone = true; return; }
                DataSnapshot lastChild = null;
                for (DataSnapshot child : snapshot.getChildren()) lastChild = child;
                if (lastChild == null) return;
                String pid = lastChild.child(FIELD_PLAYER_ID).getValue(String.class);
                String name = lastChild.child("cardName").getValue(String.class);
                Long row   = lastChild.child(FIELD_ROW).getValue(Long.class);
                Long col   = lastChild.child(FIELD_COL).getValue(Long.class);
                String meta = lastChild.child("metadata").getValue(String.class);
                Long ts    = lastChild.child(FIELD_TIMESTAMP).getValue(Long.class);
                if (pid == null || name == null) return;
                Coordinate target = (row != null && col != null) ? new Coordinate(row.intValue(), col.intValue()) : null;
                callback.onSuccess(new ActionCardSnapshot(pid, name, target, meta, ts != null ? ts : System.currentTimeMillis()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        actionCardListeners.put(roomCode, listener);
        cardsRef.addValueEventListener(listener);
    }

    @Override
    public void removeActionCardListener(String roomCode) {
        ValueEventListener listener = actionCardListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_CARDS).removeEventListener(listener);
    }

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
                if (turnPlayerId != null) callback.onSuccess(turnPlayerId);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        turnListeners.put(roomCode, listener);
        gameRef(roomCode).child(FIELD_CURRENT_TURN).addValueEventListener(listener);
    }

    @Override
    public void removeTurnListener(String roomCode) {
        ValueEventListener listener = turnListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(FIELD_CURRENT_TURN).removeEventListener(listener);
    }

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

    @Override public void sendHeartbeat(String roomCode, String playerId) {
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put(FIELD_LAST_SEEN, ServerValue.TIMESTAMP);
        gameRef(roomCode).child(NODE_HEARTBEAT).child(playerId).updateChildren(heartbeat);
        gameRef(roomCode).child(NODE_HEARTBEAT).child(playerId).child(FIELD_LAST_SEEN).onDisconnect().setValue(ServerValue.TIMESTAMP);
    }

    @Override public void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
        removeHeartbeatListener(roomCode);
        ValueEventListener listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastSeen = snapshot.child(FIELD_LAST_SEEN).getValue(Long.class);
                if (lastSeen == null) { callback.onSuccess(true); return; }
                long elapsed = System.currentTimeMillis() - lastSeen;
                callback.onSuccess(elapsed > HEARTBEAT_STALE_MS);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        heartbeatListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_HEARTBEAT).child(opponentId).addValueEventListener(listener);
    }

    @Override public void removeHeartbeatListener(String roomCode) {
        ValueEventListener listener = heartbeatListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_HEARTBEAT).removeEventListener(listener);
    }

    @Override public void sendPreview(String roomCode, String playerId, Coordinate target) {
        Map<String, Object> previewData = new HashMap<>();
        previewData.put(FIELD_ROW, target.getRow());
        previewData.put(FIELD_COL, target.getCol());
        previewData.put(FIELD_TIMESTAMP, ServerValue.TIMESTAMP);
        gameRef(roomCode).child(NODE_PREVIEW).child(playerId).updateChildren(previewData);
    }

    @Override public void clearPreview(String roomCode, String playerId) {
        gameRef(roomCode).child(NODE_PREVIEW).child(playerId).removeValue();
    }

    @Override public void addPreviewListener(String roomCode, String opponentId, DataCallback<Coordinate> callback) {
        removePreviewListener(roomCode);
        ValueEventListener listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { callback.onSuccess(null); return; }
                Long row = snapshot.child(FIELD_ROW).getValue(Long.class);
                Long col = snapshot.child(FIELD_COL).getValue(Long.class);
                if (row != null && col != null) callback.onSuccess(new Coordinate(row.intValue(), col.intValue()));
                else callback.onSuccess(null);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        previewListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_PREVIEW).child(opponentId).addValueEventListener(listener);
    }

    @Override public void removePreviewListener(String roomCode) {
        ValueEventListener listener = previewListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_PREVIEW).removeEventListener(listener);
    }

    @Override public void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback) {
        gameRef(roomCode).child(NODE_PLACEMENT_READY).child(playerId).setValue(isReady).addOnSuccessListener(unused -> callback.onSuccess(null)).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override public void addPlacementStatusListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
        removePlacementStatusListener(roomCode);
        ValueEventListener listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean ready = snapshot.getValue(Boolean.class);
                callback.onSuccess(ready != null && ready);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        placementStatusListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_PLACEMENT_READY).child(opponentId).addValueEventListener(listener);
    }

    private void removePlacementStatusListener(String roomCode) {
        ValueEventListener listener = placementStatusListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_PLACEMENT_READY).removeEventListener(listener);
    }

    @Override public void updateBoardLayout(String roomCode, String playerId, java.util.List<battleships_ex.gdx.data.ShipPlacement> ships, DataCallback<Void> callback) {
        gameRef(roomCode).child(NODE_BOARDS).child(playerId).setValue(ships).addOnSuccessListener(unused -> callback.onSuccess(null)).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override public void addBoardLayoutListener(String roomCode, String opponentId, DataCallback<java.util.List<battleships_ex.gdx.data.ShipPlacement>> callback) {
        removeBoardLayoutListener(roomCode);
        ValueEventListener listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                java.util.List<battleships_ex.gdx.data.ShipPlacement> placements = new java.util.ArrayList<>();
                for (DataSnapshot shipSnapshot : snapshot.getChildren()) {
                    battleships_ex.gdx.data.ShipPlacement placement = shipSnapshot.getValue(battleships_ex.gdx.data.ShipPlacement.class);
                    if (placement != null) placements.add(placement);
                }
                callback.onSuccess(placements);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        };
        boardLayoutListeners.put(roomCode, listener);
        gameRef(roomCode).child(NODE_BOARDS).child(opponentId).addValueEventListener(listener);
    }

    @Override public void removeBoardLayoutListener(String roomCode) {
        ValueEventListener listener = boardLayoutListeners.remove(roomCode);
        if (listener != null) gameRef(roomCode).child(NODE_BOARDS).removeEventListener(listener);
    }

    @Override public void roomIsActive(String roomCode, DataCallback<Boolean> callback) {
        gameRef(roomCode).child(FIELD_STATUS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { callback.onSuccess(false); return; }
                String status = snapshot.getValue(String.class);
                callback.onSuccess("placing".equals(status) || "playing".equals(status));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        });
    }

    @Override public void loadGameState(String roomCode, DataCallback<GameSnapshot> callback) {
        gameRef(roomCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { callback.onFailure("Game state not found"); return; }
                String currentTurn = snapshot.child(FIELD_CURRENT_TURN).getValue(String.class);
                String status      = snapshot.child(FIELD_STATUS).getValue(String.class);
                callback.onSuccess(new GameSnapshot(currentTurn, status != null ? status : "finished"));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        });
    }

    @Override public void cleanupSession(String roomCode, DataCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_STATUS, "abandoned"); // rooms/{code}/game/status
        
        gameRef(roomCode).updateChildren(updates);
        
        // Also update the root status so Lobby-based listeners see it too
        roomsRef.child(roomCode).child(FIELD_STATUS).setValue("abandoned")
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override public void removeAllListeners(String roomCode) {
        removeMoveListener(roomCode);
        removeActionCardListener(roomCode);
        removeTurnListener(roomCode);
        removeHeartbeatListener(roomCode);
        removePreviewListener(roomCode);
        removePlacementStatusListener(roomCode);
        removeBoardLayoutListener(roomCode);
        removeStatusListener(roomCode);
    }
}
