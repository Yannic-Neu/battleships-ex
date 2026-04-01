package battleships_ex.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.data.GameDataSource;
import battleships_ex.gdx.data.LobbyDataSource;
import battleships_ex.gdx.ui.Theme;
import battleships_ex.gdx.view.MenuScreen;

public class MyGame extends Game {
    public SpriteBatch batch;
    private final LobbyDataSource lobbyDataSource;
    private final GameDataSource  gameDataSource;
    private volatile String playerId;

    /**
     * @param lobbyDataSource platform-specific lobby backend (Firebase on Android, stub on Desktop)
     * @param gameDataSource  platform-specific game sync backend (Firebase on Android, stub on Desktop)
     * @param playerId unique player identifier (Firebase UID on Android, generated on Desktop)
     */
    public MyGame(LobbyDataSource lobbyDataSource, GameDataSource gameDataSource, String playerId) {
        this.lobbyDataSource = lobbyDataSource;
        this.gameDataSource  = gameDataSource;
        this.playerId = playerId;
    }

    public LobbyDataSource getLobbyDataSource() {
        return lobbyDataSource;
    }

    public GameDataSource getGameDataSource() {
        return gameDataSource;
    }

    public String getPlayerId() {
        return playerId;
    }

    /**
     * Updates the player ID after async auth completes.
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        Theme.init();
        Assets.load();

        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();

        Theme.dispose();
        Assets.dispose();

        super.dispose();
    }
}
