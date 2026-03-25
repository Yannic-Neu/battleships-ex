package battleships_ex.gdx.android.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;

/**
 * Firebase Realtime Database implementation of {@link LobbyDataSource}.
 * Uses the Firebase Android SDK for real-time synchronization.
 *
 * DB structure:
 * <pre>
 * rooms/{roomCode}/
 *   hostPlayerId:    String
 *   hostPlayerName:  String
 *   guestPlayerId:   String | null
 *   guestPlayerName: String | null
 *   status:          "waiting" | "ready" | "playing" | "finished"
 *   createdAt:       Long (server timestamp)
 * </pre>
 *
 * Name fields are written alongside their id fields so {@link LobbySnapshot}
 * can carry both without a separate profile fetch. Existing transaction logic
 * is unchanged — names are additive fields that the transaction does not need
 * to inspect for correctness.
 */
public class FirebaseLobbyDataSource implements LobbyDataSource {

    // Firebase field name constants — single source of truth.
    // Any rename is one change here, not scattered across the class.
    private static final String FIELD_HOST_ID    = "hostPlayerId";
    private static final String FIELD_HOST_NAME  = "hostPlayerName";
    private static final String FIELD_GUEST_ID   = "guestPlayerId";
    private static final String FIELD_GUEST_NAME = "guestPlayerName";
    private static final String FIELD_STATUS     = "status";
    private static final String FIELD_CREATED_AT = "createdAt";

    private static final String STATUS_WAITING  = "waiting";
    private static final String STATUS_READY    = "ready";

    private final DatabaseReference roomsRef;
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    public FirebaseLobbyDataSource() {
        this.roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
    }


    /**
     * Now writes hostPlayerName alongside hostPlayerId. Guest fields are null
     * until a player joins. No existing field is removed or renamed.
     *
     * @param roomCode      the generated room code (node key)
     * @param hostPlayerId  the host's unique id
     * @param hostPlayerName the host's display name — written to DB so the
     *                       guest can construct a proper Player on join
     * @param callback      success: null / failure: error message
     */
    @Override
    public void createLobby(String roomCode, String hostPlayerId, String hostPlayerName, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put(FIELD_HOST_ID,    hostPlayerId);
        roomData.put(FIELD_HOST_NAME,  hostPlayerName);
        roomData.put(FIELD_GUEST_ID,   null);
        roomData.put(FIELD_GUEST_NAME, null);
        roomData.put(FIELD_STATUS,     STATUS_WAITING);
        roomData.put(FIELD_CREATED_AT, System.currentTimeMillis());

        roomRef.setValue(roomData)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void joinLobby(String roomCode, String playerId, String playerName, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        roomRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    return Transaction.abort();
                }

                String status        = currentData.child(FIELD_STATUS).getValue(String.class);
                String existingGuest = currentData.child(FIELD_GUEST_ID).getValue(String.class);

                // Abort conditions unchanged from original
                if (!STATUS_WAITING.equals(status) || existingGuest != null) {
                    return Transaction.abort();
                }

                // Write guest id + name atomically with the status change
                currentData.child(FIELD_GUEST_ID).setValue(playerId);
                currentData.child(FIELD_GUEST_NAME).setValue(playerName);
                currentData.child(FIELD_STATUS).setValue(STATUS_READY);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error,
                                   boolean committed,
                                   @Nullable DataSnapshot snapshot) {
                if (error != null) {
                    callback.onFailure(error.getMessage());
                } else if (!committed) {
                    callback.onFailure("Room not available");
                } else {
                    callback.onSuccess(null);
                }
            }
        });
    }

    @Override
    public void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Room not found");
                    return;
                }

                String hostId = snapshot.child(FIELD_HOST_ID).getValue(String.class);

                if (playerId.equals(hostId)) {
                    // Host leaves: delete entire room
                    roomRef.removeValue()
                        .addOnSuccessListener(unused -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    // Guest leaves: clear both guest fields and reset status
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(FIELD_GUEST_ID,   null);
                    updates.put(FIELD_GUEST_NAME,  null);   // clear name alongside id
                    updates.put(FIELD_STATUS,      STATUS_WAITING);
                    roomRef.updateChildren(updates)
                        .addOnSuccessListener(unused -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void lobbyExists(String roomCode, DataCallback<Boolean> callback) {
        roomsRef.child(roomCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(false);
                    return;
                }
                String status = snapshot.child(FIELD_STATUS).getValue(String.class);
                callback.onSuccess(STATUS_WAITING.equals(status));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback) {
        removeLobbyListener(roomCode);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Room deleted");
                    return;
                }

                String hostId    = snapshot.child(FIELD_HOST_ID).getValue(String.class);
                String hostName  = snapshot.child(FIELD_HOST_NAME).getValue(String.class);
                String guestId   = snapshot.child(FIELD_GUEST_ID).getValue(String.class);
                String guestName = snapshot.child(FIELD_GUEST_NAME).getValue(String.class);
                String status    = snapshot.child(FIELD_STATUS).getValue(String.class);

                // Null-safe fallback for name fields — handles entries written
                // before names were added to the DB schema.
                if (hostName  == null) hostName  = "";
                if (guestName == null) guestName = "";

                callback.onSuccess(new LobbySnapshot(
                    roomCode,
                    hostId,   hostName,
                    guestId,  guestName,
                    status
                ));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        activeListeners.put(roomCode, listener);
        roomsRef.child(roomCode).addValueEventListener(listener);
    }

    @Override
    public void removeLobbyListener(String roomCode) {
        ValueEventListener listener = activeListeners.remove(roomCode);
        if (listener != null) {
            roomsRef.child(roomCode).removeEventListener(listener);
        }
    }
}
