package battleships_ex.gdx.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.android.data.FirebaseLobbyDataSource;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {

    private static final String TAG = "AndroidLauncher";
    // Set to true to connect to Firebase Local Emulator Suite for testing
    private static final boolean USE_EMULATOR = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (USE_EMULATOR) {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000);
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
            Log.d(TAG, "Using Firebase Emulator Suite");
        }

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        // Resolve player ID: use existing session or generate a temporary one.
        // Auth runs in the background; once complete, the UID is available for Firebase ops.
        String initialPlayerId;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            initialPlayerId = currentUser.getUid();
            Log.d(TAG, "Reusing existing auth session: " + initialPlayerId);
        } else {
            initialPlayerId = "pending-" + System.currentTimeMillis();
            Log.d(TAG, "Auth pending, using temporary ID");
        }

        MyGame game = new MyGame(new FirebaseLobbyDataSource(), initialPlayerId);

        // Sign in anonymously in the background; update player ID when ready
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    String uid = Objects.requireNonNull(result.getUser()).getUid();
                    Log.d(TAG, "Anonymous auth successful: " + uid);
                    game.setPlayerId(uid);
                })
                .addOnFailureListener(e ->
                    Log.e(TAG, "Anonymous auth failed", e));
        }

        // initialize() must be called synchronously in onCreate
        initialize(game, configuration);
    }
}
