package battleships_ex.gdx.lwjgl3.data;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Handles Anonymous Authentication for the Desktop client using Firebase REST.
 */
public class DesktopFirebaseAuth {
    private static final String API_KEY = loadApiKey();
    private static final String AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;

    private static String loadApiKey() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("local.properties")) {
            props.load(in);
            return props.getProperty("FIREBASE_API_KEY");
        } catch (Exception e) {
            System.err.println("Failed to load FIREBASE_API_KEY from local.properties: " + e.getMessage());
            return "";
        }
    }

    public static class AuthResult {
        public String uid;
        public String idToken;
    }

    public static AuthResult signInAnonymously() throws Exception {
        URL url = new URL(AUTH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Request anonymous sign-in
        String payload = "{\"returnSecureToken\":true}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() >= 400) {
            throw new RuntimeException("Auth failed: HTTP " + conn.getResponseCode());
        }

        // Parse the response using libGDX's JsonReader
        JsonReader jsonReader = new JsonReader();
        JsonValue responseJson = jsonReader.parse(new InputStreamReader(conn.getInputStream()));

        AuthResult result = new AuthResult();
        result.uid = responseJson.getString("localId");
        result.idToken = responseJson.getString("idToken");

        return result;
    }
}
