package battleships_ex.gdx.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseAuth;

import battleships_ex.gdx.MyGame;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {

    private static final String TAG = "AndroidLauncher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sign in anonymously so Firebase security rules (auth != null) are satisfied
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener(result ->
                Log.d(TAG, "Anonymous auth successful: " + result.getUser().getUid()))
            .addOnFailureListener(e ->
                Log.e(TAG, "Anonymous auth failed", e));

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(new MyGame(new FirebaseLobbyDataSource()), configuration);
    }
}
