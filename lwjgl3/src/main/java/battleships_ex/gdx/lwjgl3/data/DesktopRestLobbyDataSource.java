package battleships_ex.gdx.lwjgl3.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DesktopRestLobbyDataSource implements LobbyDataSource {

    private final String baseUrl = "https://battleshipsex-8968a-default-rtdb.europe-west1.firebasedatabase.app/rooms";
    private final String idToken;
    private Timer pollTimer;

    public DesktopRestLobbyDataSource(String idToken) {
        this.idToken = idToken;
    }

    private String buildUrl(String roomCode) {
        return baseUrl + "/" + roomCode + ".json?auth=" + idToken;
    }

    @Override
    public void createLobby(String roomCode, String hostPlayerId, String hostPlayerName, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);

                long ts = System.currentTimeMillis();
                String payload = String.format(Locale.ROOT,
                    "{\"hostPlayerId\":\"%s\",\"hostPlayerName\":\"%s\",\"guestReady\":false,\"exModeEnabled\":true,\"status\":\"waiting\",\"createdAt\":%d}",
                    hostPlayerId, hostPlayerName, ts);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(null));
                } else {
                    Gdx.app.postRunnable(() -> callback.onFailure("Failed to create lobby."));
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
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setDoOutput(true);

                String payload = String.format(Locale.ROOT,
                    "{\"guestPlayerId\":\"%s\",\"guestPlayerName\":\"%s\",\"status\":\"joined\"}",
                    playerId, playerName);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(null));
                } else {
                    Gdx.app.postRunnable(() -> callback.onFailure("Failed to join lobby."));
                }
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void leaveLobby(String roomCode, String playerId, DataCallback<Void> callback) {
    }

    @Override
    public void lobbyExists(String roomCode, DataCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    JsonValue root = new JsonReader().parse(conn.getInputStream());
                    Gdx.app.postRunnable(() -> callback.onSuccess(root != null && !root.isNull()));
                } else {
                    Gdx.app.postRunnable(() -> callback.onSuccess(false));
                }
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }

    @Override
    public void addLobbyListener(String roomCode, DataCallback<LobbySnapshot> callback) {
        if (pollTimer != null) pollTimer.cancel();
        pollTimer = new Timer(true);
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL(buildUrl(roomCode));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        JsonValue snapshot = new JsonReader().parse(conn.getInputStream());
                        if (snapshot == null || snapshot.isNull()) return;

                        JsonValue selectedCardsJson = snapshot.get("selectedCardNames");
                        java.util.ArrayList<String> selectedCards = new java.util.ArrayList<>();
                        if (selectedCardsJson != null && selectedCardsJson.isArray()) {
                            for (JsonValue card : selectedCardsJson) {
                                selectedCards.add(card.asString());
                            }
                        }

                        LobbySnapshot data = new LobbySnapshot(
                            roomCode,
                            snapshot.getString("hostPlayerId", ""),
                            snapshot.getString("hostPlayerName", ""),
                            snapshot.getString("guestPlayerId", null),
                            snapshot.getString("guestPlayerName", null),
                            snapshot.getString("status", "waiting"),
                            snapshot.getBoolean("guestReady", false),
                            snapshot.getBoolean("exModeEnabled", true),
                            selectedCards
                        );

                        Gdx.app.postRunnable(() -> callback.onSuccess(data));
                    }
                } catch (Exception ignored) {}
            }
        }, 0, 2000);
    }

    @Override
    public void removeLobbyListener(String roomCode) {
        if (pollTimer != null) {
            pollTimer.cancel();
            pollTimer = null;
        }
    }

    @Override
    public void setGuestReady(String roomCode, boolean ready, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setDoOutput(true);
                String payload = String.format(Locale.ROOT, "{\"guestReady\":%b}", ready);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void setLobbyStatus(String roomCode, String status, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setDoOutput(true);
                String payload = String.format(Locale.ROOT, "{\"status\":\"%s\"}", status);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void setExMode(String roomCode, boolean enabled, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setDoOutput(true);
                String payload = String.format(Locale.ROOT, "{\"exModeEnabled\":%b}", enabled);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void setSelectedCards(String roomCode, java.util.List<String> cardNames, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setDoOutput(true);

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < cardNames.size(); i++) {
                    sb.append("\"").append(cardNames.get(i)).append("\"");
                    if (i < cardNames.size() - 1) sb.append(",");
                }
                sb.append("]");

                String payload = String.format(Locale.ROOT, "{\"selectedCardNames\":%s}", sb);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }
}
