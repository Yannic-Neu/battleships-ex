package battleships_ex.gdx.lwjgl3.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.model.board.Coordinate;

/**
 * Desktop implementation of {@link GameDataSource} using Firebase REST API.
 * Uses polling for real-time updates since the Desktop JVM lacks the Firebase SDK.
 */
public class DesktopRestGameDataSource implements GameDataSource {

    private static final String DB_URL = "https://battleships-ex-default-rtdb.europe-west1.firebasedatabase.app";
    private final String idToken;

    private final Map<String, ScheduledExecutorService> pollers = new HashMap<>();

    public DesktopRestGameDataSource(String idToken) {
        this.idToken = idToken;
    }

    private String buildUrl(String roomCode, String path) {
        return DB_URL + "/rooms/" + roomCode + "/game/" + path + ".json?auth=" + idToken;
    }

    @Override
    public void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "placementReady/" + playerId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);

                String payload = String.valueOf(isReady);
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
    public void addPlacementStatusListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {
        String key = "placement_" + roomCode + "_" + opponentId;
        removePoller(key);

        ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        pollers.put(key, poller);

        poller.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "placementReady/" + opponentId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue snapshot = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));

                boolean ready = snapshot != null && snapshot.asBoolean();
                Gdx.app.postRunnable(() -> callback.onSuccess(ready));
            } catch (Exception e) {
                // Ignore transient network errors
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void updateGameStatus(String roomCode, String status, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "status"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);

                String payload = "\"" + status + "\"";
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
    public void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "currentTurn"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);

                String payload = "\"" + currentPlayerId + "\"";
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
    public void submitMove(String roomCode, String playerId, Coordinate target, boolean hit, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "moves"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String payload = String.format(Locale.ROOT,
                    "{\"playerId\":\"%s\",\"row\":%d,\"col\":%d,\"hit\":%b,\"timestamp\":%d}",
                    playerId, target.getRow(), target.getCol(), hit, System.currentTimeMillis()
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

    @Override public void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback) {
        // TODO: Implement move polling for battle phase
    }

    @Override public void removeMoveListener(String roomCode) {
        removePoller("moves_" + roomCode);
    }

    @Override public void addTurnListener(String roomCode, DataCallback<String> callback) {
        // TODO: Implement turn polling
    }

    @Override public void removeTurnListener(String roomCode) {
        removePoller("turn_" + roomCode);
    }

    @Override public void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback) {
        // Handled by updateGameStatus + additional field logic if needed
    }

    @Override public void sendHeartbeat(String roomCode, String playerId) {}
    @Override public void addHeartbeatListener(String roomCode, String opponentId, DataCallback<Boolean> callback) {}
    @Override public void removeHeartbeatListener(String roomCode) {}
    @Override public void sendPreview(String roomCode, String playerId, Coordinate target) {}
    @Override public void clearPreview(String roomCode, String playerId) {}
    @Override public void addPreviewListener(String roomCode, String opponentId, DataCallback<Coordinate> callback) {}
    @Override public void removePreviewListener(String roomCode) {}
    @Override public void roomIsActive(String roomCode, DataCallback<Boolean> callback) {}
    @Override public void loadGameState(String roomCode, DataCallback<GameSnapshot> callback) {}
    @Override public void cleanupSession(String roomCode, DataCallback<Void> callback) {}

    @Override
    public void removeAllListeners(String roomCode) {
        for (String key : new java.util.HashSet<>(pollers.keySet())) {
            if (key.contains(roomCode)) {
                removePoller(key);
            }
        }
    }

    private void removePoller(String key) {
        ScheduledExecutorService poller = pollers.remove(key);
        if (poller != null) {
            poller.shutdownNow();
        }
    }
}
