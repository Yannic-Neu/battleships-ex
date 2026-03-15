package battleships_ex.gdx.android;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;

/**
 * Firebase Realtime Database implementation of {@link LobbyDataSource}.
 * Stores lobbies under /rooms/{roomCode} matching the database.rules.json schema.
 */
public class FirebaseLobbyDataSource implements LobbyDataSource {

    private final DatabaseReference roomsRef;
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    public FirebaseLobbyDataSource() {
        roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
    }

    @Override
    public void createLobby(String roomCode, String hostPlayerId, DataCallback<Void> callback) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("hostPlayerId", hostPlayerId);
        roomData.put("status", "waiting");
        roomData.put("createdAt", System.currentTimeMillis());

        roomsRef.child(roomCode).setValue(roomData)
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void joinLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        DatabaseReference roomRef = roomsRef.child(roomCode);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Room not found");
                    return;
                }

                String existingGuest = snapshot.child("guestPlayerId").getValue(String.class);
                if (existingGuest != null) {
                    callback.onFailure("Room is full");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("guestPlayerId", playerId);
                updates.put("status", "ready");

                roomRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        roomsRef.child(roomCode).removeValue()
            .addOnSuccessListener(unused -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
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
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

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
