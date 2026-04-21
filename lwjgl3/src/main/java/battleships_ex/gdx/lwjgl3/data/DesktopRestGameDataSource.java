package battleships_ex.gdx.lwjgl3.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.ShipPlacement;
import battleships_ex.gdx.model.board.Coordinate;

/**
 * Desktop implementation of {@link GameDataSource} using Firebase REST API.
 * Uses polling for real-time updates since the Desktop JVM lacks the Firebase SDK.
 */
public class DesktopRestGameDataSource implements GameDataSource {

    private static final String DB_URL = "https://battleshipsex-8968a-default-rtdb.europe-west1.firebasedatabase.app";
    private final String idToken;
    private final Map<String, ScheduledExecutorService> pollers = new HashMap<>();

    public DesktopRestGameDataSource(String idToken) {
        this.idToken = idToken;
    }

    private String buildUrl(String roomCode, String path) {
        return DB_URL + "/rooms/" + roomCode + "/game/" + path + ".json?auth=" + idToken;
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
                    playerId, target.getRow(), target.getCol(), hit, System.currentTimeMillis());
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void addMoveListener(String roomCode, DataCallback<MoveSnapshot> callback) {
        String key = "moves_" + roomCode;
        removePoller(key);
        ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        pollers.put(key, poller);
        poller.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "moves"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue root = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));
                if (root == null || root.isNull() || root.size == 0) return;
                JsonValue last = null;
                for (JsonValue child : root) last = child;
                if (last == null) return;
                MoveSnapshot snap = new MoveSnapshot(last.getString("playerId"), last.getInt("row"), last.getInt("col"), last.getBoolean("hit"), last.getLong("timestamp", 0));
                Gdx.app.postRunnable(() -> callback.onSuccess(snap));
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void removeMoveListener(String roomCode) {
        removePoller("moves_" + roomCode);
    }

    @Override
    public void submitActionCardPlay(String roomCode, String playerId, String cardName, Coordinate target, String metadata, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "cards"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                StringBuilder sb = new StringBuilder("{");
                sb.append("\"playerId\":\"").append(playerId).append("\",");
                sb.append("\"cardName\":\"").append(cardName).append("\",");
                if (target != null) {
                    sb.append("\"row\":").append(target.getRow()).append(",");
                    sb.append("\"col\":").append(target.getCol()).append(",");
                }
                if (metadata != null) sb.append("\"metadata\":\"").append(metadata).append("\",");
                sb.append("\"timestamp\":").append(System.currentTimeMillis()).append("}");
                try (OutputStream os = conn.getOutputStream()) { os.write(sb.toString().getBytes(StandardCharsets.UTF_8)); }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void addActionCardListener(String roomCode, DataCallback<ActionCardSnapshot> callback) {
        String key = "cards_" + roomCode;
        removePoller(key);
        ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        pollers.put(key, poller);
        poller.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "cards"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue root = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));
                if (root == null || root.isNull() || root.size == 0) return;
                JsonValue last = null;
                for (JsonValue child : root) last = child;
                if (last == null) return;
                Coordinate target = null;
                if (last.has("row") && last.has("col")) target = new Coordinate(last.getInt("row"), last.getInt("col"));
                ActionCardSnapshot snap = new ActionCardSnapshot(last.getString("playerId"), last.getString("cardName"), target, last.getString("metadata", null), last.getLong("timestamp", 0));
                Gdx.app.postRunnable(() -> callback.onSuccess(snap));
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void removeActionCardListener(String roomCode) {
        removePoller("cards_" + roomCode);
    }

    @Override
    public void syncTurn(String roomCode, String currentPlayerId, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "currentTurn"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { os.write(("\"" + currentPlayerId + "\"").getBytes(StandardCharsets.UTF_8)); }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void addTurnListener(String roomCode, DataCallback<String> callback) {
        String key = "turn_" + roomCode;
        removePoller(key);
        ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        pollers.put(key, poller);
        poller.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "currentTurn"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JsonValue val = new JsonReader().parse(new InputStreamReader(conn.getInputStream()));
                if (val != null && !val.isNull()) Gdx.app.postRunnable(() -> callback.onSuccess(val.asString()));
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void removeTurnListener(String roomCode) {
        removePoller("turn_" + roomCode);
    }

    @Override
    public void updateGameStatus(String roomCode, String status, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "status"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { os.write(("\"" + status + "\"").getBytes(StandardCharsets.UTF_8)); }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
        }).start();
    }

    @Override
    public void addStatusListener(String roomCode, DataCallback<String> callback) {

    }

    @Override
    public void updatePlacementStatus(String roomCode, String playerId, boolean isReady, DataCallback<Void> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(buildUrl(roomCode, "placementReady/" + playerId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { os.write(String.valueOf(isReady).getBytes(StandardCharsets.UTF_8)); }
                if (conn.getResponseCode() == 200) Gdx.app.postRunnable(() -> callback.onSuccess(null));
            } catch (Exception e) { Gdx.app.postRunnable(() -> callback.onFailure(e.getMessage())); }
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
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override public void updateBoardLayout(String roomCode, String playerId, List<ShipPlacement> ships, DataCallback<Void> callback) {}
    @Override public void addBoardLayoutListener(String roomCode, String opponentId, DataCallback<List<ShipPlacement>> callback) {}
    @Override public void removeBoardLayoutListener(String roomCode) {}
    @Override public void pushGameOver(String roomCode, String winnerName, DataCallback<Void> callback) {}
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
            if (key.contains(roomCode)) removePoller(key);
        }
    }

    private void removePoller(String key) {
        ScheduledExecutorService poller = pollers.remove(key);
        if (poller != null) poller.shutdownNow();
    }
}
