package battleships_ex.gdx.lwjgl3.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;

public class DesktopRestLobbyDataSource implements LobbyDataSource {

    private static final String DB_URL = "https://battleships-ex-default-rtdb.europe-west1.firebasedatabase.app";
    private final String idToken;

    private ScheduledExecutorService poller;
    private boolean isListening = false;

    public DesktopRestLobbyDataSource(String idToken) {
        this.idToken = idToken;
    }

    private String buildUrl(String roomCode) {
        return DB_URL + "/rooms/" + roomCode + ".json?auth=" + idToken;
    }

    @Override
    public void createLobby(String roomCode, String hostPlayerId, String hostPlayerName, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Build JSON payload
                String payload = String.format(Locale.ROOT,
                    "{\"hostPlayerId\":\"%s\",\"hostPlayerName\":\"%s\",\"status\":\"waiting\",\"createdAt\":%d}",
                    hostPlayerId, hostPlayerName, System.currentTimeMillis()
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(null));
                } else {
                    Gdx.app.postRunnable(() -> {
                        try {
                            callback.onFailure("HTTP " + conn.getResponseCode());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void joinLobby(String roomCode, String playerId, String playerName, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                // 1. Check if room is available
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection checkConn = (HttpURLConnection) url.openConnection();
                JsonValue roomData = new JsonReader().parse(new InputStreamReader(checkConn.getInputStream()));

                if (roomData == null || !roomData.getString("status", "").equals("waiting") || roomData.has("guestPlayerId")) {
                    Gdx.app.postRunnable(() -> callback.onFailure("Room not available"));
                    return;
                }

                // 2. Patch the room to join
                HttpURLConnection patchConn = (HttpURLConnection) url.openConnection();
                patchConn.setRequestMethod("POST"); // Standard HTTP method for partial updates
                patchConn.setRequestProperty("X-HTTP-Method-Override", "PATCH"); // Fallback for strict firewalls
                patchConn.setRequestProperty("Content-Type", "application/json");
                patchConn.setDoOutput(true);

                String payload = String.format(Locale.ROOT,
                    "{\"guestPlayerId\":\"%s\",\"guestPlayerName\":\"%s\",\"status\":\"ready\"}",
                    playerId, playerName
                );

                try (OutputStream os = patchConn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (patchConn.getResponseCode() == 200) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(null));
                } else {
                    Gdx.app.postRunnable(() -> callback.onFailure("Failed to join."));
                }
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void lobbyExists(String roomCode, DataCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue roomData = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));

                boolean exists = roomData != null && "waiting".equals(roomData.getString("status", ""));
                Gdx.app.postRunnable(() -> callback.onSuccess(exists));
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback) {
        // Simple implementation: for desktop testing, we will just delete the room if leaving
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.getResponseCode();
                Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback) {
        removeLobbyListener(roomCode);
        isListening = true;
        poller = Executors.newSingleThreadScheduledExecutor();

        // Poll Firebase every 1.5 seconds for updates
        poller.scheduleAtFixedRate(() -> {
            if (!isListening) return;
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue snapshot = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));

                if (snapshot == null || snapshot.isNull()) {
                    Gdx.app.postRunnable(() -> callback.onFailure("Room deleted"));
                    return;
                }

                LobbySnapshot data = new LobbySnapshot(
                    roomCode,
                    snapshot.getString("hostPlayerId", ""),
                    snapshot.getString("hostPlayerName", ""),
                    snapshot.getString("guestPlayerId", null),
                    snapshot.getString("guestPlayerName", null),
                    snapshot.getString("status", "waiting")
                );

                Gdx.app.postRunnable(() -> callback.onSuccess(data));

            } catch (Exception e) {
                // Ignore temporary network drops during polling
            }
        }, 0, 1500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void removeLobbyListener(String roomCode) {
        isListening = false;
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
    }
}
