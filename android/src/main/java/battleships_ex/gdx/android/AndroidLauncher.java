package battleships_ex.gdx.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.android.data.FirebaseLobbyDataSource;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {

    private static final String TAG = "AndroidLauncher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        // Check if already signed in (persisted session)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Reusing existing auth session: " + currentUser.getUid());
            startGame(configuration, currentUser.getUid());
        } else {
            // Sign in anonymously so Firebase security rules (auth != null) are satisfied
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    Log.d(TAG, "Anonymous auth successful: " + uid);
                    startGame(configuration, uid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Anonymous auth failed, starting with stub", e);
                    // Fallback: start without Firebase connectivity
                    startGame(configuration, "offline-" + System.currentTimeMillis());
                });
        }
    }

    private void startGame(AndroidApplicationConfiguration config, String playerId) {
        MyGame game = new MyGame(new FirebaseLobbyDataSource(), playerId);
        initialize(game, config);
    }
}
