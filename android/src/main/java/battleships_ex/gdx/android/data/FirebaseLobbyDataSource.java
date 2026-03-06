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
 *   hostPlayerId: String
 *   guestPlayerId: String | null
 *   status: "waiting" | "ready" | "playing" | "finished"
 *   createdAt: Long (server timestamp)
 * </pre>
 */
public class FirebaseLobbyDataSource implements LobbyDataSource {

    private final DatabaseReference roomsRef;
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    public FirebaseLobbyDataSource() {
        this.roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
    }

    @Override
    public void createLobby(String roomCode, String hostPlayerId, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("hostPlayerId", hostPlayerId);
        roomData.put("guestPlayerId", null);
        roomData.put("status", "waiting");
        roomData.put("createdAt", System.currentTimeMillis());

        roomRef.setValue(roomData)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void joinLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        // Use transaction to prevent race conditions (two players joining at once)
        roomRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    return Transaction.abort();
                }

                String status = currentData.child("status").getValue(String.class);
                String existingGuest = currentData.child("guestPlayerId").getValue(String.class);

                if (!"waiting".equals(status) || existingGuest != null) {
                    return Transaction.abort();
                }

                currentData.child("guestPlayerId").setValue(playerId);
                currentData.child("status").setValue("ready");
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
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

                String hostId = snapshot.child("hostPlayerId").getValue(String.class);

                if (playerId.equals(hostId)) {
                    // Host leaves: delete entire room
                    roomRef.removeValue()
                        .addOnSuccessListener(unused -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    // Guest leaves: reset to waiting
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("guestPlayerId", null);
                    updates.put("status", "waiting");
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
                String status = snapshot.child("status").getValue(String.class);
                callback.onSuccess("waiting".equals(status));
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

                String host = snapshot.child("hostPlayerId").getValue(String.class);
                String guest = snapshot.child("guestPlayerId").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                callback.onSuccess(new LobbySnapshot(roomCode, host, guest, status));
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
